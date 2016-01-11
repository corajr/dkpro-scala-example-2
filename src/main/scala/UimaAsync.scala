import org.apache.uima.UIMAFramework
import org.apache.uima.aae.client.UimaAsynchronousEngine
import org.apache.uima.adapter.jms.client.BaseUIMAAsynchronousEngine_impl
import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import org.apache.uima.resourceSpecifier.factory.SerializationStrategy
import org.apache.uima.resourceSpecifier.factory.DeploymentDescriptorFactory
import org.apache.uima.resourceSpecifier.factory.impl.ServiceContextImpl
import org.apache.activemq.broker.BrokerService

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

object UimaUtil {
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

case class UimaAsyncDeploymentConfig(
  engineDescs: Seq[AnalysisEngineDescription],
  appCtx: UimaAppContext = UimaAppContext(),
  name: String = "",
  description: String = "",
  descriptor: String = ""
  ) {
  def toXML(): String = {
    val serviceCtx = new ServiceContextImpl(name, description, descriptor, appCtx.endpoint)
    val deployDescriptor = DeploymentDescriptorFactory.createAggregateDeploymentDescriptor(
      serviceCtx
    )
    
    val engineDescriptor = createEngineDescription(engineDescs: _*)
    val engineXML = UimaUtil.tmpWriter { out => engineDescriptor.toXML(out) }
    
    deployDescriptor.setCasPoolSize(appCtx.casPoolSize)
    deployDescriptor.setServiceAnalysisEngineDescriptor(engineXML)
    UimaUtil.tmpFile(deployDescriptor.save(_))
  }
}


class UimaAsync(val config: UimaAppContext = UimaAppContext()) {
  val engine: UimaAsynchronousEngine = new BaseUIMAAsynchronousEngine_impl
  val configMap = config.toMap
  var springContainerId: Option[String] = None
  
  def start(corpus: Corpus, process: Process): Unit = {
    if (UimaAsync.broker.isEmpty) {
      throw new IllegalStateException("Broker must be started before instantiating UimaAsync")
    }

    val collectionReader = UIMAFramework.produceCollectionReader(corpus.reader)
    engine.setCollectionReader(collectionReader)
    
    val deployConfig = UimaAsyncDeploymentConfig(engineDescs = process.engines, appCtx = config)
    val deployXML = deployConfig.toXML()
        
    springContainerId = Some(engine.deploy(deployXML, configMap))
    
    engine.initialize(configMap)
    engine.process()

    springContainerId.foreach { springId => engine.undeploy(springId) }
    engine.stop()
  }
}

object UimaAsync {
  var broker: Option[BrokerService] = None
  
  def start(): Unit = {
    val broker = new BrokerService
    broker.setBrokerName("localhost")
    broker.setUseJmx(false)
    broker.start()
    this.broker = Some(broker)
  }

  def stop(): Unit = broker match {
    case Some(b) => b.stop()
    case None => ()
  }
}