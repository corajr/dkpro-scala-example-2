package uimaAS

import org.scalatest._
import org.scalamock.scalatest.MockFactory
import org.apache.uima.aae.client.UimaAsynchronousEngine

class UimaStatusCallbackListenerSpec extends FunSpec with Matchers with MockFactory {
  describe("UimaStatusCallbackListener") {
    describe("initializationComplete") {
      describe("when called normally") {
        it("should do nothing") {
          val engineMock = mock[UimaAsynchronousEngine]
          val statusListener = new UimaStatusCallbackListener(engineMock)
          val status = null
          statusListener.initializationComplete(status)
        }
      }
      describe("when called with an error") {
        it("should do nothing") {
        }
      }
    }
  }
}