package uimaAS

import org.scalatest._
import prop._
import org.scalacheck.{Arbitrary, Gen}
import org.apache.uima.UIMAFramework
import org.apache.uima.aae.client.UimaAsynchronousEngine
import org.apache.uima.resourceSpecifier.factory.SerializationStrategy
import java.net.{URI, URL}

trait UimaAppContextGens {
  implicit val url = Arbitrary(for {
    proto <- Gen.oneOf("http", "ftp", "file")
    host <- Gen.identifier
    file <- Gen.identifier
  } yield new URL(proto, host, file))

  implicit val uri = Arbitrary(for {
    path <- Gen.identifier
  } yield new URI("file", path, null))

  val appCtxs = for {
    dd2SpringXsltFilePath <- url.arbitrary
    saxonClasspath <- url.arbitrary
    serverUri <- uri.arbitrary
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
}

class UimaAppContextSpec extends FunSpec with Matchers with PropertyChecks with UimaAppContextGens {
  describe("UimaAppContext") {
    it("should have sensible defaults") {
      val appCtx = UimaAppContext()
      appCtx.dd2SpringXsltFilePath.toString should not be empty
      appCtx.saxonClasspath.toString should not be empty
    }

    it("should return its settings as a java.util.Map") {
      forAll(appCtxs) { appCtx =>
        val map = appCtx.toMap
        map.get(UimaAsynchronousEngine.DD2SpringXsltFilePath) shouldBe appCtx.dd2SpringXsltFilePath.toString
        map.get(UimaAsynchronousEngine.SaxonClasspath) shouldBe appCtx.saxonClasspath.toString
        map.get(UimaAsynchronousEngine.ServerUri) shouldBe appCtx.serverUri.toString
        map.get(UimaAsynchronousEngine.ENDPOINT) shouldBe appCtx.endpoint
        map.get(UimaAsynchronousEngine.CasPoolSize) shouldBe appCtx.casPoolSize
        map.get(org.apache.uima.UIMAFramework.CAS_INITIAL_HEAP_SIZE) shouldBe appCtx.casInitialHeapSize.toString
        map.get(UimaAsynchronousEngine.ApplicationName) shouldBe appCtx.applicationName
        map.get(UimaAsynchronousEngine.GetMetaTimeout) shouldBe appCtx.getMetaTimeout
        map.get(UimaAsynchronousEngine.SERIALIZATION_STRATEGY) shouldBe appCtx.serializationStrategy.name()
        appCtx.timeout match {
          case Some(t) => map.get(UimaAsynchronousEngine.Timeout) shouldBe t
          case None    => map should not(contain key UimaAsynchronousEngine.Timeout)
        }
        appCtx.cpcTimeout match {
          case Some(t) => map.get(UimaAsynchronousEngine.CpcTimeout) shouldBe t
          case None    => map should not(contain key UimaAsynchronousEngine.CpcTimeout)
        }
      }
    }
  }
}
