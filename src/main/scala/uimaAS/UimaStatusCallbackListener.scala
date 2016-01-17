package uimaAS
import org.apache.uima.aae.client.UimaAsBaseCallbackListener
import org.apache.uima.aae.client.UimaASProcessStatus
import org.apache.uima.aae.client.UimaAsynchronousEngine
import org.apache.uima.collection.EntityProcessStatus
import org.apache.uima.cas.CAS
import org.apache.uima.jcas.JCas
import scala.collection.concurrent
import scala.concurrent.{ Future, Promise }
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.ConcurrentLinkedQueue
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class UimaStatusCallbackListener[T](
    val engine: UimaAsynchronousEngine,
    val block: Util.Block[T] = Util.noOp,
    val maybeOutputDir: Option[String] = None,
    val collectionTotal: Option[Int] = None,
    val logCas: Boolean = true) extends UimaAsBaseCallbackListener {
  import collection.JavaConversions._

  val startTime = System.nanoTime() / 1000000
  val casMap: concurrent.Map[String, Long] = concurrent.TrieMap()
  val queue = new ConcurrentLinkedQueue[Util.TaggedAnalysis[T]]()
  val promisedCompletion = Promise[Unit]
  val promisedResults = Promise[Util.Results[T]]

  val logger = Logger(LoggerFactory.getLogger(""))

  var entityCount: Int = 0
  var size: Long = 0

  val outputDir: Util.OutputDir =
    maybeOutputDir.fold(Util.TmpDir.create())(Util.UserSpecifiedDir)

  def stopOnErr(status: EntityProcessStatus, msg: String, ignoreErrors: Boolean = false)(block: => Unit): Unit = {
    if (status != null && status.isException()) {
      logger.error(msg)
      val exceptions = status.getExceptions()
      for (e <- exceptions) {
        logger.error(e.toString)
      }

      if (!ignoreErrors) {
        logger.error("Terminating Client...")
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
      if (collectionTotal.nonEmpty) {
        Await.ready(promisedCompletion.future, Duration.Inf)
      }
      promisedResults.success(queue.toMap)
      //      promisedIterator.success(queue.iterator().map { path =>
      //        val cas = engine.getCAS()
      //        Util.deserializeCasBinary(cas, path)
      //      })

      logger.info(s"Completed $entityCount documents")
      if (size > 0) {
        logger.info(s"; $size characters")
      }
      val elapsedTime = System.nanoTime() / 1000000 - startTime
      logger.info("Time Elapsed : " + elapsedTime + " ms ");

      val perfReport = engine.getPerformanceReport()
      if (perfReport != null) {
        logger.info("\n\n ------------------ PERFORMANCE REPORT ------------------\n");
        logger.info(perfReport);
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
            logger.debug(s"$ip \t $start \t ${current - start}")
          }
        } else {
          logger.debug(".");
          if (0 == (entityCount + 1) % 50) {
            logger.info((entityCount + 1) + " processed\n");
          }
        }
      }

      //      val serialized = Util.serializeCasBinary(cas, outputDir.path, Some(entityCount))

      entityCount = entityCount + 1

      val docText = cas.getDocumentText
      if (docText != null) {
        size += docText.length
      }

      queue.add(block(cas.getJCas))
      collectionTotal.foreach { total =>
        if (entityCount == total) {
          promisedCompletion.success(Unit)
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
