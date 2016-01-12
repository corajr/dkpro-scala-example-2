package uimaAS
import org.apache.uima.aae.client.UimaAsBaseCallbackListener
import org.apache.uima.aae.client.UimaASProcessStatus
import org.apache.uima.aae.client.UimaAsynchronousEngine
import org.apache.uima.collection.EntityProcessStatus
import org.apache.uima.cas.CAS
import scala.collection.concurrent
import java.util.concurrent.ConcurrentLinkedQueue

class UimaStatusCallbackListener(
    val engine: UimaAsynchronousEngine,
    val outputDir: Option[String] = None) extends UimaAsBaseCallbackListener {
  import collection.JavaConversions._

  val startTime = System.nanoTime() / 1000000
  var entityCount: Int = 0
  var size: Long = 0
  var logCas: Boolean = true
  val casMap: concurrent.Map[String, Long] = concurrent.TrieMap()

  def stopOnErr(status: EntityProcessStatus, msg: String, ignoreErrors: Boolean = false)(block: => Unit): Unit = {
    if (status != null && status.isException()) {
      System.err.println(msg)
      val exceptions = status.getExceptions()
      for (e <- exceptions) {
        e.printStackTrace()
      }

      if (!ignoreErrors) {
        System.err.println("Terminating Client...")
        engine.stop()
      } else {
        block
      }
    } else {
      block
    }
  }

  override def initializationComplete(status: EntityProcessStatus): Unit =
    stopOnErr(status, "Error on getMeta call to remote service:") {}

  override def collectionProcessComplete(status: EntityProcessStatus): Unit =
    stopOnErr(status, "Error on collection process complete call to remote service:") {
      System.out.print("Completed " + entityCount + " documents")
      if (size > 0) {
        System.out.print("; " + size + " characters");
      }
      System.out.println();
      val elapsedTime = System.nanoTime() / 1000000 - startTime
      System.out.println("Time Elapsed : " + elapsedTime + " ms ");

      val perfReport = engine.getPerformanceReport()
      if (perfReport != null) {
        System.out.println("\n\n ------------------ PERFORMANCE REPORT ------------------\n");
        System.out.println(perfReport);
      }
    }

  override def entityProcessComplete(cas: CAS, status: EntityProcessStatus): Unit =
    stopOnErr(status, "Error on process CAS call to remote service:", true) {
      if (logCas) {
        var ip: Option[String] = None
        val events = status.getProcessTrace.getEventsByComponentName("UimaEE", false)
        for (event <- events) {
          if (event.getDescription().equals("Service IP")) {
            ip = Some(event.getResultMessage())
          }
        }

        val casId = status.asInstanceOf[UimaASProcessStatus].getCasReferenceId
        if (casId != null) {
          val current = System.nanoTime() / 1000000 - startTime
          casMap.get(casId).foreach { start =>
            System.out.println(ip + "\t" + start + "\t" + (current - start))
          }
        } else {
          System.out.print(".");
          if (0 == (entityCount + 1) % 50) {
            System.out.print((entityCount + 1) + " processed\n");
          }
        }

        outputDir.foreach { dir =>
          Util.serializeCas(cas, dir, entityCount)
        }

        entityCount = entityCount + 1
        val docText = cas.getDocumentText
        if (docText != null) {
          size += docText.length
        }
      }
    }

  override def onBeforeMessageSend(status: UimaASProcessStatus): Unit = {
    val current = System.nanoTime() / 1000000 - startTime
    casMap.put(status.getCasReferenceId, current)
  }

  override def onBeforeProcessCAS(status: UimaASProcessStatus, nodeIP: String, pid: String): Unit = {
  }
}
