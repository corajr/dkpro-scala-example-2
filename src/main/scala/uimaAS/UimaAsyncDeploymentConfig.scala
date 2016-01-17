package uimaAS

import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import org.apache.uima.resourceSpecifier.factory.impl.ServiceContextImpl
import org.apache.uima.resourceSpecifier.factory.DeploymentDescriptorFactory
import org.apache.uima.resourceSpecifier.factory.UimaASDeploymentDescriptor
import org.apache.uima.resourceSpecifier.factory.ServiceContext
import org.apache.uima.resourceSpecifier.factory.DelegateConfiguration
import org.apache.uima.resourceSpecifier.AnalysisEngineDeploymentDescriptionDocument
import org.apache.uima.resourceSpecifier.factory.DeploymentDescriptor
import org.apache.uima.resourceSpecifier.factory.UimaASAggregateDeploymentDescriptor
import org.apache.uima.resourceSpecifier.factory.impl.UimaASAggregateDeploymentDescriptorImpl
import org.apache.uima.resourceSpecifier.factory.impl.DeploymentDescriptorImpl

case class UimaAsyncDeploymentConfig(
    engineDescs: Seq[AnalysisEngineDescription],
    appCtx: UimaAppContext = UimaAppContext(),
    name: String = "uima-service",
    description: String = "",
    descriptor: String = "",
    async: Boolean = true) {
  def toXML(): String = {
    val serviceCtx = new ServiceContextImpl(name, description, descriptor, appCtx.endpoint)
    serviceCtx.setScaleup(appCtx.casPoolSize)
    val deployDescriptor = new EnhancedAggregateDeploymentDescriptor(serviceCtx)

    val engineDescriptor = createEngineDescription(engineDescs: _*)
    engineDescriptor.setAnnotatorImplementationName(name)
    val engineXML = Util.tmpWriter { out => engineDescriptor.toXML(out) }

    deployDescriptor.setBroker(appCtx.serverUri.toString)
    deployDescriptor.setInitialHeapSize(appCtx.casInitialHeapSize * 4)
    deployDescriptor.setCasPoolSize(appCtx.casPoolSize)
    deployDescriptor.setServiceAnalysisEngineName(name)
    deployDescriptor.setServiceAnalysisEngineDescriptor(engineXML)
    deployDescriptor.setAsync(async)
    Util.tmpFile(deployDescriptor.save(_))
  }
}

class EnhancedAggregateDeploymentDescriptor(context: ServiceContext)
    extends UimaASAggregateDeploymentDescriptorImpl(AnalysisEngineDeploymentDescriptionDocument.Factory.newInstance(), context) {
  def setServiceAnalysisEngineName(name: String): Unit = {
    getDeploymentDescriptor().getDeployment().getService().getTopDescriptor().getImport().setByName(name)
  }
}
