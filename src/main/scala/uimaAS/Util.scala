package uimaAS

import java.io._
import scala.util.Try
import org.apache.uima.cas.CAS
import org.apache.uima.cas.impl.XmiCasSerializer

object Util {
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
  
  def serializeCas(cas: CAS, outputDir: String, entityCount: Long = 0): Unit = {
    // try to retrieve the filename of the input file from the CAS
    val outFile = (for {
      srcDocInfoType <- Option(cas.getTypeSystem().getType(
        "org.apache.uima.examples.SourceDocumentInformation"))
      it = cas.getIndexRepository().getAllIndexedFS(srcDocInfoType)
      srcDocInfoFs <- if (it.hasNext()) Some(it.get) else None
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
        XmiCasSerializer.serialize(cas, outStream)
      } finally {
        outStream.close();
      }
    } catch {
      case e: Exception =>
        System.err.println("Could not save CAS to XMI file");
        e.printStackTrace();
    }
  }
}