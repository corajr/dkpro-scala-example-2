package uimaAS

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import org.apache.uima.resourceSpecifier.factory.impl.ServiceContextImpl
import org.apache.uima.resourceSpecifier.factory.DeploymentDescriptorFactory

case class UimaAsyncDeploymentConfig(
  engineDescs: Seq[AnalysisEngineDescription],
  appCtx: UimaAppContext = UimaAppContext(),
  name: String = "",
  description: String = "",
  descriptor: String = "",
  async: Boolean = true
  ) {
  def toXML(): String = {
    val serviceCtx = new ServiceContextImpl(name, description, descriptor, appCtx.endpoint)
    val deployDescriptor = DeploymentDescriptorFactory.createAggregateDeploymentDescriptor(
      serviceCtx
    )
    
    val engineDescriptor = createEngineDescription(engineDescs: _*)
    val engineXML = Util.tmpWriter { out => engineDescriptor.toXML(out) }
    
    deployDescriptor.setInitialHeapSize(appCtx.casInitialHeapSize * 4)
    deployDescriptor.setCasPoolSize(appCtx.casPoolSize)
    deployDescriptor.setServiceAnalysisEngineDescriptor(engineXML)
    deployDescriptor.setAsync(async)
    Util.tmpFile(deployDescriptor.save(_))
  }
}
