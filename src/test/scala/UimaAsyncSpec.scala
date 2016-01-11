import org.scalatest._
import prop._
import org.scalacheck.Gen
import org.apache.uima.aae.client.UimaAsynchronousEngine
import org.apache.uima.cas.SerialFormat
import org.apache.uima.resourceSpecifier.factory.SerializationStrategy
import scala.xml.XML

class UimaAsyncSpec extends FunSpec with Matchers with PropertyChecks {
  val corpusDir =
    getClass().getClassLoader().getResource("inaugural/").getPath()

  def withBroker[T](block: => T): T = {
    UimaAsync.start()
    try block
    finally UimaAsync.stop()
  }
  
  describe("UimaAsync") {
    describe("when constructed") {
      it("should create the UimaAsynchronousEngine but not initialize it") {
        val uimaAsync = new UimaAsync()
        uimaAsync.engine.getMetaData shouldBe null
      }
    }
    describe("on calling `start(corpus, process)`") {
      it("should process the corpus, returning results in an iterator") {
        
      }
    }

  }
}
