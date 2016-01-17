package uimaAS

import org.apache.uima.resourceSpecifier.factory.SerializationStrategy
import org.apache.uima.aae.client.UimaAsynchronousEngine
import org.apache.uima.UIMAFramework
import java.net.{URI, URL}

case class UimaAppContext(
  dd2SpringXsltFilePath: URL = getClass.getClassLoader.getResource("dd2spring.xsl"),
  saxonClasspath: URL = getClass.getClassLoader.getResource("saxon8.jar"),
  serverUri: URI = new URI("tcp://localhost:61616"),
  endpoint: String = "uimaAS",
  casPoolSize: Int = 2,
  casInitialHeapSize: Int = 500000, // number of 4-byte words = 2000000 bytes (2 MB)
  applicationName: String = "",
  getMetaTimeout: Int = 60000,
  timeout: Option[Int] = None,
  cpcTimeout: Option[Int] = None,
  serializationStrategy: SerializationStrategy = SerializationStrategy.binary
) {
  import collection.JavaConversions.mutableMapAsJavaMap

  def toMap: java.util.Map[String, Object] = mutableMapAsJavaMap({
    val m: collection.mutable.Map[String, Object] = collection.mutable.HashMap(
      UimaAsynchronousEngine.DD2SpringXsltFilePath -> dd2SpringXsltFilePath.toString,
      UimaAsynchronousEngine.SaxonClasspath -> saxonClasspath.toString,
      UimaAsynchronousEngine.ServerUri -> serverUri.toString,
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
