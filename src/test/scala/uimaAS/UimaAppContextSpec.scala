package uimaAS

import org.scalatest._
import prop._
import org.scalacheck.Gen
import org.apache.uima.UIMAFramework
import org.apache.uima.aae.client.UimaAsynchronousEngine

class UimaAppContextSpec extends FunSpec with Matchers with PropertyChecks {
  describe("UimaAppContext") {
    it("should have sensible defaults") {
      val appCtx = UimaAppContext()
      appCtx.dd2SpringXsltFilePath should not be empty
      appCtx.saxonClasspath should not be empty
    }

    val appCtxs = Gen.resultOf(UimaAppContext)
    it("should return its settings as a java.util.Map") {
      forAll(appCtxs) { appCtx =>
        val map = appCtx.toMap
        map.get(UimaAsynchronousEngine.DD2SpringXsltFilePath) shouldBe appCtx.dd2SpringXsltFilePath
        map.get(UimaAsynchronousEngine.SaxonClasspath) shouldBe appCtx.saxonClasspath
        map.get(UimaAsynchronousEngine.ServerUri) shouldBe appCtx.serverUri
        map.get(UimaAsynchronousEngine.ENDPOINT) shouldBe appCtx.endpoint
        map.get(UimaAsynchronousEngine.CasPoolSize) shouldBe appCtx.casPoolSize
        map.get(org.apache.uima.UIMAFramework.CAS_INITIAL_HEAP_SIZE) shouldBe appCtx.casInitialHeapSize.toString
        map.get(UimaAsynchronousEngine.ApplicationName) shouldBe appCtx.applicationName
        map.get(UimaAsynchronousEngine.GetMetaTimeout) shouldBe appCtx.getMetaTimeout
        map.get(UimaAsynchronousEngine.SERIALIZATION_STRATEGY) shouldBe appCtx.serializationStrategy.name()
        appCtx.timeout match {
          case Some(t) => map.get(UimaAsynchronousEngine.Timeout) shouldBe t
          case None => map should not (contain key UimaAsynchronousEngine.Timeout)
        }
        appCtx.cpcTimeout match {
          case Some(t) => map.get(UimaAsynchronousEngine.CpcTimeout) shouldBe t
          case None => map should not (contain key UimaAsynchronousEngine.CpcTimeout)
        }
      }
    }
  }
}