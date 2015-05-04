package v1.test.util

import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import scala.concurrent.Future
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse
import v1.client.SCM
import v1.client.Github
import play.api.Application
import play.api.test.FakeApplication
import play.api.test.Helpers._
import play.api.libs.ws.WSRequestHolder

object MockitoUtils extends MockitoSugar {

  /**
   * Creates a successfull/failed mocked WSResponse
   */
  def mockFutureWSResponse[T](result: T, success: Boolean = true): Future[WSResponse] = {
    val wsResponse = mock[WSResponse]
    val jsValue = mock[JsValue]
    val jsResult: JsResult[T] = new JsSuccess(result, null)

    when(jsValue.validate[T](anyObject())).thenReturn(jsResult)
    when(wsResponse.json).thenReturn(jsValue)
    success match {
      case true  => Future.successful(wsResponse)
      case false => Future.failed(new RuntimeException)
    }
  }

  /**
   *
   * Github creates an implementation of the Github client
   * where the method requestHolder is overridden to always return the passed `WSRequestHolder`,
   * which can be mocked.
   *
   */
  def github(requestParam: ((String) => WSRequestHolder)) = new Github {
    override def requestHolder =  requestParam 
  }

  def withFakeApplication(block: => Unit): Unit = {
    running(FakeApplication()) {
      block
    }
  }

}