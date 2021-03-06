import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.SpanSugar._
import prop._
import org.scalacheck.Gen
import scala.xml.XML

import org.apache.uima.aae.client.UimaAsynchronousEngine
import org.apache.uima.cas.SerialFormat
import org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription
import org.apache.uima.resourceSpecifier.factory.SerializationStrategy

trait BrokerFixture extends BeforeAndAfterAll { this: Suite =>
  override def beforeAll() {
    UimaAsync.start()
  }

  override def afterAll() {
    UimaAsync.stop()
  }
}

class UimaAsyncSpec extends FunSpec with Matchers with PropertyChecks with BrokerFixture with ScalaFutures {
  val corpusDir =
    getClass().getClassLoader().getResource("inaugural/").getPath()

  describe("UimaAsync") {
    describe("when constructed") {
      it("should create the UimaAsynchronousEngine but not initialize it") {
        val uimaAsync = new UimaAsync()
        uimaAsync.engine.getMetaData shouldBe null
      }
    }
    describe("when constructed with custom options") {
      it("should create the UimaAsynchronousEngine but not initialize it") {
        val uimaAsync = new UimaAsync()
        uimaAsync.engine.getMetaData shouldBe null
      }
    }

    describe("on calling `start(corpus, process)`") {
      it("should process the corpus, returning results as a Future[Map[String, T]]") {
        val uimaAsync = new UimaAsync()
        val dummyDesc = createEngineDescription(classOf[uimaAS.DummyAE])
        val futureResults = uimaAsync.start(Corpus.fromDir(corpusDir), Process(dummyDesc), uimaAS.Util.noOp)
        whenReady(futureResults, timeout(1 minute)) { it =>
          it.size shouldBe 56
        }
      }
    }

  }
}
