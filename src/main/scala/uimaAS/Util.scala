package uimaAS

object Util {
  def tmpFile(block: java.io.File => Unit): String = {
    val configFile = java.io.File.createTempFile("uimaTemp", ".xml")
    configFile.deleteOnExit()

    block(configFile)

    configFile.toString
  }
  
  def tmpWriter(block: java.io.Writer => Unit): String = tmpFile { configFile =>
    val out = new java.io.BufferedWriter(new java.io.FileWriter(configFile))
    try block(out)
    finally out.close()    
  }
  
  def toXmlFile[T <: { def toXML(): String }](t: T): String = tmpWriter { out =>    
    out.write(t.toXML())
  }
}