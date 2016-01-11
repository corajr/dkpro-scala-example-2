package uimaAS

import org.scalatest._
import prop._
import org.scalacheck.Gen
import org.apache.uima.aae.client.UimaAsynchronousEngine
import org.apache.uima.cas.SerialFormat
import org.apache.uima.resourceSpecifier.factory.SerializationStrategy
import scala.xml.XML

class UimaAsyncDeploymentConfigSpec extends FunSpec with Matchers with PropertyChecks {
  describe("UimaAsyncDeploymentConfig") {
    it("should generate an XML file with default config") {
      val appCtx = UimaAppContext()
      val config = UimaAsyncDeploymentConfig(Seq(), appCtx = appCtx)
      val fname = config.toXML()
      val xml = XML.loadFile(fname)
      
      val deploy = xml \\ "analysisEngineDeploymentDescription" \\ "deployment" 
      (deploy \\ "casPool" \@ "numberOfCASes") shouldBe "1"
      (deploy \\ "service" \\ "inputQueue" \@ "endpoint") shouldBe appCtx.endpoint

      val engineXML = deploy \\ "service" \\ "topDescriptor" \\ "import" \@ "location"
      new java.io.File(engineXML) shouldBe 'exists
    }

    val appCtxs = for {
      dd2SpringXsltFilePath <- Gen.alphaStr
      saxonClasspath <- Gen.alphaStr
      serverUri <- Gen.alphaStr
      endpoint <- Gen.alphaStr
      casPoolSize <- Gen.posNum[Int]
      casInitialHeapSize <- Gen.posNum[Int]
      applicationName <- Gen.alphaStr
      getMetaTimeout <- Gen.posNum[Int]
      timeout <- Gen.option(Gen.posNum[Int])
      cpcTimeout <- Gen.option(Gen.posNum[Int])
      serializationStrategy <- Gen.oneOf(SerializationStrategy.values())    
    } yield UimaAppContext(dd2SpringXsltFilePath = dd2SpringXsltFilePath,
        saxonClasspath = saxonClasspath,
        serverUri = serverUri,
        endpoint = endpoint,
        casPoolSize = casPoolSize,
        casInitialHeapSize = casInitialHeapSize,
        applicationName = applicationName,
        getMetaTimeout = getMetaTimeout,
        timeout = timeout,
        cpcTimeout = cpcTimeout,
        serializationStrategy = serializationStrategy)
     
    it("should generate an XML file with specified config") {
      forAll(appCtxs) { appCtx =>
        val config = UimaAsyncDeploymentConfig(Seq(), appCtx = appCtx)
        val fname = config.toXML()
        val xml = XML.loadFile(fname)    
        val deploy = xml \\ "analysisEngineDeploymentDescription" \\ "deployment" 
        (deploy \\ "casPool" \@ "numberOfCASes") shouldBe appCtx.casPoolSize.toString
        (deploy \\ "service" \\ "inputQueue" \@ "endpoint") shouldBe appCtx.endpoint
        val engineXML = deploy \\ "service" \\ "topDescriptor" \\ "import" \@ "location"
        new java.io.File(engineXML) shouldBe 'exists
      }
    }
  }  
}