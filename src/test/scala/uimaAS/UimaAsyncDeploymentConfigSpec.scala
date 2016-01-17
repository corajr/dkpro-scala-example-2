package uimaAS

import org.scalatest._
import prop._
import org.scalacheck.Gen
import org.apache.uima.aae.client.UimaAsynchronousEngine
import org.apache.uima.cas.SerialFormat
import org.apache.uima.resourceSpecifier.factory.SerializationStrategy
import scala.xml.XML

class UimaAsyncDeploymentConfigSpec extends FunSpec with Matchers with PropertyChecks with UimaAppContextGens {
  describe("UimaAsyncDeploymentConfig") {
    it("should generate an XML file with default config") {
      val appCtx = UimaAppContext()
      val config = UimaAsyncDeploymentConfig(Seq(), appCtx = appCtx)
      val fname = config.toXML()
      val xml = XML.loadFile(fname)

      (xml \\ "name" text) should not be empty

      val deploy = xml \\ "analysisEngineDeploymentDescription" \\ "deployment"
      (deploy \\ "casPool" \@ "numberOfCASes") shouldBe appCtx.casPoolSize.toString
      (deploy \\ "service" \\ "inputQueue" \@ "endpoint") shouldBe appCtx.endpoint

      val engineXML = deploy \\ "service" \\ "topDescriptor" \\ "import" \@ "location"
      new java.io.File(engineXML) shouldBe 'exists
      deploy \\ "service" \\ "topDescriptor" \\ "import" \@ "name" should not be empty
    }

    it("should generate an XML file with specified config") {
      forAll(appCtxs) { appCtx =>
        val config = UimaAsyncDeploymentConfig(Seq(), appCtx = appCtx)
        val fname = config.toXML()
        val xml = XML.loadFile(fname)
        (xml \\ "name" text) should not be empty

        val deploy = xml \\ "analysisEngineDeploymentDescription" \\ "deployment"
        (deploy \\ "casPool" \@ "numberOfCASes") shouldBe appCtx.casPoolSize.toString
        (deploy \\ "casPool" \@ "initialFsHeapSize") shouldBe (appCtx.casInitialHeapSize * 4).toString
        (deploy \\ "service" \\ "inputQueue" \@ "brokerURL") shouldBe appCtx.serverUri.toString
        (deploy \\ "service" \\ "inputQueue" \@ "endpoint") shouldBe appCtx.endpoint
        val engineXML = deploy \\ "service" \\ "topDescriptor" \\ "import" \@ "location"
        new java.io.File(engineXML) shouldBe 'exists
        deploy \\ "service" \\ "topDescriptor" \\ "import" \@ "name" should not be empty
      }
    }
  }
}
