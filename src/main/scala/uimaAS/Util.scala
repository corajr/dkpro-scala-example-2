package uimaAS

import java.io._
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger
import scala.util.Try
import org.apache.uima.cas.CAS
import org.apache.uima.cas.impl.XmiCasSerializer
import org.apache.uima.cas.impl.XmiCasDeserializer
import org.apache.uima.cas.impl.Serialization
import org.apache.uima.examples.SourceDocumentInformation
import org.apache.uima.jcas.JCas

object Util {
  sealed trait OutputDir {
    def path: String
  }
  case class TmpDir(path: String) extends OutputDir
  case class UserSpecifiedDir(path: String) extends OutputDir

  object TmpDir {
    def create(): OutputDir = {
      val tmpDir = Files.createTempDirectory("uima-CASes")
      TmpDir(tmpDir.toString)
    }
  }

  type TaggedAnalysis[T] = (String, T)
  type Block[T] = JCas => TaggedAnalysis[T]
  type Results[T] = Map[String, T]

  private val uniqueID = new AtomicInteger()

  def getUniqueID: Int = uniqueID.getAndIncrement

  val noOp: Block[Unit] = { jcas => getUniqueID.toString -> Unit }

  def tmpFile(block: File => Unit): String = {
    val configFile = File.createTempFile("uimaTemp", ".xml")
    configFile.deleteOnExit()

    block(configFile)

    configFile.toString
  }

  def tmpWriter(block: java.io.Writer => Unit): String = tmpFile { configFile =>
    val out = new java.io.BufferedWriter(new FileWriter(configFile))
    try block(out)
    finally out.close()
  }

  def toXmlFile[T <: { def toXML(): String }](t: T): String = tmpWriter { out =>
    out.write(t.toXML())
  }

  def serializeCasXMI(cas: CAS, outputDir: String, entityCount: Option[Long] = None): String =
    serializeCas(cas, outputDir, entityCount)(XmiCasSerializer.serialize _)

  def serializeCasBinary(cas: CAS, outputDir: String, entityCount: Option[Long] = None): String =
    serializeCas(cas, outputDir, entityCount)(Serialization.serializeCAS _)

  def serializeCas(cas: CAS, outputDir: String, entityCount: Option[Long])(block: (CAS, OutputStream) => Unit): String = {
    // try to retrieve the filename of the input file from the CAS
    val outFile = (for {
      srcDocInfoType <- Option(cas.getTypeSystem().getType(
        "org.apache.uima.examples.SourceDocumentInformation"))
      it = cas.getIndexRepository().getAllIndexedFS(srcDocInfoType)
      srcDocInfoFs: SourceDocumentInformation <- if (it.hasNext()) Some(it.get) else None
      uriFeat = srcDocInfoType.getFeatureByBaseName("uri")
      offsetInSourceFeat = srcDocInfoType.getFeatureByBaseName("offsetInSource")
      uri = srcDocInfoFs.getStringValue(uriFeat)
      offsetInSource = srcDocInfoFs.getIntValue(offsetInSourceFeat)
      namedOutFile <- Try {
        val inFile = new File(new java.net.URL(uri).getPath())
        val outFileName = new StringBuilder(inFile.getName())
        if (offsetInSource > 0) {
          outFileName ++= "_" + offsetInSource
        }
        outFileName ++= ".xmi"
        new File(outputDir, outFileName.toString())
      }.toOption
    } yield namedOutFile).getOrElse(new File(outputDir, "doc" + entityCount))

    try {
      val outStream = new FileOutputStream(outFile)
      try {
        block(cas, outStream)
      } finally {
        outStream.close();
      }
    } catch {
      case e: Exception =>
        System.err.println("Could not save CAS to XMI file");
        e.printStackTrace();
    }

    outFile.getPath
  }

  def deserializeCasBinary(cas: CAS, path: String): JCas =
    deserializeCas(cas, path) { (cas, istream) =>
      Serialization.deserializeCAS(cas, istream)
    }

  def deserializeCasXMI(cas: CAS, path: String): JCas =
    deserializeCas(cas, path) { (cas, istream) =>
      XmiCasDeserializer.deserialize(istream, cas)
    }

  def deserializeCas(cas: CAS, path: String)(block: (CAS, InputStream) => Unit): JCas = {
    val istream = new FileInputStream(path)
    block(cas, istream)
    cas.getJCas
  }
}
