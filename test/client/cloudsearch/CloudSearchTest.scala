package client.cloudsearch

import org.scalatest.mock.MockitoSugar
import test.util.MockitoUtils
import play.api.test.FakeApplication
import play.api.libs.json.Json
import org.scalatest.FlatSpec
import play.api.libs.ws.WSRequestHolder
import client.RequestDispatcher
import org.mockito.Mockito._
import org.mockito.Matchers._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import play.api.libs.json.Format
import org.scalatest.BeforeAndAfter
import play.api.http.ContentTypeOf
import play.api.http.Writeable
import org.mockito.ArgumentCaptor
import model._
import test.util.FakeResponseData

/**
 * @author fbenjamin
 */
class CloudSearchTest extends FlatSpec with MockitoSugar with MockitoUtils with BeforeAndAfter {

  private val dispatcher = mock[RequestDispatcher]
  private val requestHolder = mock[WSRequestHolder]

  private val appsDocEndpointUrl = "appsDocEndpoint"
  private val appsSearchEndpointUrl = "appsSearchEndpoint"
  private val commitsDocEndpointUrl = "commitsDocEndpoint"
  private val commitsSearchEndpointUrl = "commitsSearchEndpoint"
  private val config = new CloudSearchConfigurationImpl {
    override lazy val appsDocEndpoint = Future.successful(appsDocEndpointUrl)
    override lazy val appsSearchEndpoint = Future.successful(appsSearchEndpointUrl)
    override lazy val commitsSearchEndpoint = Future.successful(commitsSearchEndpointUrl)
    override lazy val commitsDocEndpoint = Future.successful(commitsDocEndpointUrl)

  }
  private val client: DocumentStore = new CloudSearchImpl(config, dispatcher)
  before(reset(dispatcher, requestHolder))

  "DocumentStore#saveCommits" should "post to appsDocEndpoint with Json objects" in {
    val author1 = createAuthor("name1", "email1", List())
    val author2 = createAuthor("name2", "email2", List())
    val commit1 = createCommit("id1", "message1", List("parentId1", "parentId1"), author1, Some(true), List())
    val commit2 = createCommit("id2", "message2", List("parentId2", "parentId2"), author1, Some(true), List())
    val commits = List(commit1, commit2)
    val app = new AppInfo("scmUrl", "serviceUrl", "created", "lastModified1")
    val result = testBulkUploads(commitsDocEndpointUrl, client.saveCommits(app, commits))
    assert(result)
  }

  "DocumentStore#uploadAppInfos" should "post to appsDocEndpoint with Json objects" in {
    val appInfo1 = new AppInfo("scmUrl1", "serviceUrl1", "created1", "lastModified1")
    val appInfo2 = new AppInfo("scmUrl2", "serviceUrl2", "created2", "lastModified2")
    val apps = List(appInfo1, appInfo2)
    val result = testBulkUploads(appsDocEndpointUrl, client.saveAppInfos(apps))
    assert(result)
  }

  "DocumentStore#appInfos" should "retrieve from appsSearchEndpoint" in {
    val mockedWSResponse = createMockedWSResponse(FakeResponseData.cloudSearchAppsResult, 200)
    val response = Future.successful(mockedWSResponse)

    when(dispatcher.requestHolder(anyString)).thenReturn(requestHolder)
    when(requestHolder.withQueryString(("q", "*.*"))).thenReturn(requestHolder)
    when(requestHolder.get()).thenReturn(response)

    val result = Await.result(client.appInfos(), Duration("50 seconds"))

    verify(dispatcher, times(1)).requestHolder(s"http://$appsSearchEndpointUrl/2013-01-01/search")
    verify(requestHolder, times(1)).withQueryString(("q", "*.*"))
    verify(requestHolder, times(1)).get()
  }

  "DocumentStore#commits" should "post to AppInfosEndpoint with Json objects" in {
    val appInfo1 = new AppInfo("scmUrl1", "serviceUrl1", "created1", "lastModified1")
    val appInfo2 = new AppInfo("scmUrl2", "serviceUrl2", "created2", "lastModified2")
    val apps = List(appInfo1, appInfo2)
    val result = testBulkUploads(appsDocEndpointUrl, client.saveAppInfos(apps))
    assert(result)
  }

  def testBulkUploads[T](url: String, call: => Future[Boolean]): Boolean = {
    val mockedWSResponse = createMockedWSResponse("", 200)
    val response = Future.successful(mockedWSResponse)

    when(dispatcher.requestHolder(url)).thenReturn(requestHolder)
    when(requestHolder.withHeaders("Content-Type" -> "application/json")).thenReturn(requestHolder)

    implicit val writable = any[Writeable[String]]
    implicit val contentTypeOf = any[ContentTypeOf[String]]

    when(requestHolder.post(anyString)).thenReturn(response)

    val result = Await.result(call, Duration("5 seconds"))
    verify(dispatcher, times(1)).requestHolder(url)
    verify(requestHolder, times(1)).withHeaders("Content-Type" -> "application/json")

    val payload = ArgumentCaptor.forClass(classOf[String])
    verify(requestHolder, times(1)).post(payload.capture())(any[Writeable[String]], any[ContentTypeOf[String]])

    result
  }
}