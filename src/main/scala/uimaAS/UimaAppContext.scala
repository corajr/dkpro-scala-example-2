package uimaAS

import org.apache.uima.resourceSpecifier.factory.SerializationStrategy
import org.apache.uima.aae.client.UimaAsynchronousEngine
import org.apache.uima.UIMAFramework

case class UimaAppContext(
  dd2SpringXsltFilePath: String = getClass.getClassLoader.getResource("dd2spring.xsl").getPath,
  saxonClasspath: String = getClass.getClassLoader.getResource("saxon8.jar").getPath,
  serverUri: String = "vm://localhost?create=false",
  endpoint: String = "uimaAS",
  casPoolSize: Int = 1,
  casInitialHeapSize: Int = 500000,
  applicationName: String = "",
  getMetaTimeout: Int = 60000,
  timeout: Option[Int] = None,
  cpcTimeout: Option[Int] = None,
  serializationStrategy: SerializationStrategy = SerializationStrategy.xmi
) {
  import collection.JavaConversions.mutableMapAsJavaMap

  def toMap: java.util.Map[String, Object] = mutableMapAsJavaMap({
    val m: collection.mutable.Map[String, Object] = collection.mutable.HashMap(
      UimaAsynchronousEngine.DD2SpringXsltFilePath -> dd2SpringXsltFilePath,
      UimaAsynchronousEngine.SaxonClasspath -> saxonClasspath,
      UimaAsynchronousEngine.ServerUri -> serverUri,
      UimaAsynchronousEngine.ENDPOINT -> endpoint,
      UimaAsynchronousEngine.CasPoolSize -> int2Integer(casPoolSize),
      UIMAFramework.CAS_INITIAL_HEAP_SIZE -> casInitialHeapSize.toString,
      UimaAsynchronousEngine.ApplicationName -> applicationName,
      UimaAsynchronousEngine.GetMetaTimeout -> int2Integer(getMetaTimeout),
      UimaAsynchronousEngine.SERIALIZATION_STRATEGY -> serializationStrategy.name()
    )
    timeout.fold() { t =>
      m += UimaAsynchronousEngine.Timeout -> int2Integer(t)
    }
    cpcTimeout.fold() { t =>
      m += UimaAsynchronousEngine.CpcTimeout -> int2Integer(t)
    }

    m
  })
}