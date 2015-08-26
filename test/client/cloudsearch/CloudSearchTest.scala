package client.cloudsearch

import org.scalatestplus.play.PlaySpec
import org.scalatest.mock.MockitoSugar
import test.util.MockitoUtils
import play.api.test.WithApplication
import play.api.test.FakeApplication
import play.api.test._
import play.api.test.Helpers._
import client.RequestDispatcherImpl
import play.api.libs.json.Json
import model.AppInfo
import org.scalatest.FlatSpec
import play.api.libs.ws.WSRequestHolder
import client.RequestDispatcher
import org.mockito.Mockito._
import org.mockito.Matchers._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import model.KontrollettiToJsonParser
import model.KontrollettiToModelParser
import play.api.libs.json.Format
import org.scalatest.BeforeAndAfter
import play.api.http.ContentTypeOf
import play.api.http.Writeable
import org.mockito.ArgumentCaptor
import model._

/**
 * @author fbenjamin
 */
class CloudSearchTest extends FlatSpec with MockitoSugar with MockitoUtils with BeforeAndAfter {

  private val dispatcher = mock[RequestDispatcher]
  private val requestHolder = mock[WSRequestHolder]
  private val mockedWSResponse = createMockedWSResponse("", 200)
  private val mockedResponse = Future.successful(mockedWSResponse)

  private val config = new CloudSearchConfigurationImpl {
    override lazy val appsDocEndpoint = Some("appsEndpoint")
    		override lazy val appsSearchEndpoint = Some("appsEndpoint")
    
  }
  private val client = new CloudSearchImpl(config, dispatcher)

   

  "CloudSearch#uploadAppInfos" should "post to AppInfosEndpoint with Json objects" in {
    val appInfo1 = new AppInfo("scmUrl1", "serviceUrl1", "created1", "lastModified1")
    val appInfo2 = new AppInfo("scmUrl2", "serviceUrl2", "created2", "lastModified2")
    val apps = List(appInfo1, appInfo2)

    val result = testUploads(config.appsDocEndpoint, client.uploadAppInfos(apps))
    assert(result)

  }
  
  "CloudSearch#appInfos" should "retrieve from appInfosSearchEdnpoint" in {
//    when(dispatcher.requestHolder(url))
    
    
  }

  def testUploads[T](someUrl: Option[String], call: => Future[Boolean]): Boolean = {
    val Some(url) = someUrl

    when(dispatcher.requestHolder(url)).thenReturn(requestHolder)
    when(requestHolder.withHeaders("Content-Type" -> "application/json")).thenReturn(requestHolder)

    implicit val writable = any[Writeable[String]]
    implicit val contentTypeOf = any[ContentTypeOf[String]]

    when(requestHolder.post(anyString)).thenReturn(mockedResponse)

    val result = Await.result(call, Duration("5 seconds"))
    verify(dispatcher, times(1)).requestHolder(url)
    verify(requestHolder, times(1)).withHeaders("Content-Type" -> "application/json")

    val payload = ArgumentCaptor.forClass(classOf[String])
    verify(requestHolder, times(1)).post(payload.capture())(any[Writeable[String]], any[ContentTypeOf[String]])

    result
  }
}