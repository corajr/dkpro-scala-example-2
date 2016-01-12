package uimaAS

import org.scalatest._
import org.scalatest.mock.JMockCycle
import org.apache.uima.aae.client.UimaAsynchronousEngine
import org.apache.uima.collection.EntityProcessStatus
import org.jmock.api.Action
import org.jmock.lib.action.ReturnValueAction
import org.jmock.lib.action.ReturnIteratorAction
import org.apache.uima.aae.client.UimaAsBaseCallbackListener

class UimaStatusCallbackListenerSpec extends FunSpec {
  def returnValue[T](result: T): Action =
    new ReturnValueAction(result)

  def emptyList[T]: java.util.List[T] = new java.util.LinkedList[T]()

  def expectStopOnErr(cycle: JMockCycle)
                     (block: (UimaAsBaseCallbackListener, EntityProcessStatus) => Unit): Unit = {
    import cycle._

    val engineMock = mock[UimaAsynchronousEngine]
    val statusMock = mock[EntityProcessStatus]

    expecting { e => import e._
      oneOf (statusMock).isException(); will(returnValue(true))
      oneOf (statusMock).getExceptions(); will(returnValue(emptyList[Exception]))
      oneOf (engineMock).stop()
    }
    val statusListener = new UimaStatusCallbackListener(engineMock)
    whenExecuting(block(statusListener, statusMock))
  }

  describe("UimaStatusCallbackListener") {
    describe("initializationComplete") {
      describe("when called normally") {
        it("should do nothing") {
          val cycle = new JMockCycle
          import cycle._

          val engineMock = mock[UimaAsynchronousEngine]
          expecting { e => import e._
          }

          val statusListener = new UimaStatusCallbackListener(engineMock)
          val status = null

          whenExecuting {
            statusListener.initializationComplete(status)
          }
        }
      }
      describe("when called with an error") {
        it("should call stop on the engine") {
          val cycle = new JMockCycle
          expectStopOnErr(cycle) { (statusListener, status) =>
            statusListener.initializationComplete(status)
          }
        }
      }
    }

    describe("collectionProcessComplete") {
      describe("when called normally") {
        it("should do nothing") {
          val cycle = new JMockCycle
          import cycle._

          val engineMock = mock[UimaAsynchronousEngine]
          expecting { e => import e._
            oneOf (engineMock).getPerformanceReport(); will(returnValue(null))
          }

          val statusListener = new UimaStatusCallbackListener(engineMock)
          val status = null

          whenExecuting {
            statusListener.collectionProcessComplete(status)
          }
        }
      }
      describe("when called with an error") {
        it("should call stop on the engine") {
          val cycle = new JMockCycle
          expectStopOnErr(cycle) { (statusListener, status) =>
            statusListener.collectionProcessComplete(status)
          }
        }
      }
    }
  }
}
