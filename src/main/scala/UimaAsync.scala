import org.apache.uima.UIMAFramework
import org.apache.uima.aae.client.UimaAsynchronousEngine
import org.apache.uima.adapter.jms.client.BaseUIMAAsynchronousEngine_impl
import org.apache.uima.resourceSpecifier.factory.SerializationStrategy
import org.apache.activemq.broker.BrokerService

import uimaAS._

class UimaAsync(val config: UimaAppContext = UimaAppContext()) {
  val engine: UimaAsynchronousEngine = new BaseUIMAAsynchronousEngine_impl
  val statusListener = new UimaStatusCallbackListener(this.engine)
  val configMap = config.toMap
  var springContainerId: Option[String] = None
  
  def start(corpus: Corpus, process: Process): Unit = {
    if (UimaAsync.broker.isEmpty) {
      throw new IllegalStateException("Broker must be started before instantiating UimaAsync")
    }

    val collectionReader = UIMAFramework.produceCollectionReader(corpus.reader)
    engine.setCollectionReader(collectionReader)
    engine.addStatusCallbackListener(statusListener)
    
    val deployConfig = UimaAsyncDeploymentConfig(engineDescs = process.engines, appCtx = config)
    val deployXML = deployConfig.toXML()
        
    springContainerId = Some(engine.deploy(deployXML, configMap))
    
    engine.initialize(configMap)
    engine.process()
  }
  
  def stop(): Unit = {
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

  def stop(): Unit = broker.foreach { b => b.stop() }
}