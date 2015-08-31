package job

import scala.concurrent.Future
import org.mockito.Mockito.when
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import client.oauth.OAuth
import test.util.MockitoUtils
import org.mockito.Mockito._
import org.mockito.Matchers._
import client.kio.Kio
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import jobs.ImportImpl
import javax.inject.Inject
import javax.inject.Singleton
import scala.Right
import service.DataStore
import service.Search

class ImportTest extends FlatSpec with MockitoSugar with MockitoUtils {

  private val kioClient = mock[Kio]
  private val oAuthClient = mock[OAuth]
  private val store = mock[DataStore]
  private val search = mock[Search]
  private val synchronizer = new ImportImpl(oAuthClient, store, kioClient, search)

  "Synchronizer#syncApps" should "store apps from kio in data-store" in {

    val accessToken = createOAuthAccessToken("token_type", "access_token", "scope", 3599)
    val accessTokenResult = Future.successful { accessToken }
    val appIds = List("kontrolletti", "cloud-lobster")
    val appIdsResult = Future.successful(appIds)
    val validAppInfo = createAppInfo("https://git-hub.com/zalando-bus/kontrolletti/", "specUrl1", "docUrl1", "serviceUrl1", "created1", "lastModified1")
    val unvalidAppInfo = createAppInfo("scmUrl2", "specUrl2", "docUrl2", "serviceUrl2", "created2", "lastModified2")
    val apps = List(validAppInfo, unvalidAppInfo)
    val appsResult = Future.successful(apps)

    when(oAuthClient.accessToken()).thenReturn(accessTokenResult)
    when(kioClient.apps(accessToken)).thenReturn(appsResult)
    when(store.saveAppInfo(anyObject())).thenReturn(Future.successful(true))

    Await.result(synchronizer.syncApps(), Duration("5 seconds")) match {
      case value: Boolean => assert(value == true)
    }

    verify(oAuthClient, times(1)).accessToken()
    verify(kioClient, times(1)).apps(accessToken)
    verify(store, times(1)).saveAppInfo(List(validAppInfo))
  }

  "Synchronizer#synchCommits" should "store apps from kio in data-store" in {
    val url1 = "https://github.com/zalando/kontrolletti/"
    val url2 = "https://git-hub.com/zalando-bus/lizzy/"
    val scmUrls = List(url1, url2)
    val commit1 = createCommit()
    val commit2 = createCommit(id = "id2")
    val commit3 = createCommit(id = "id3")
    val commitResult1 = Future.successful(Right(Some(List(commit1, commit2))))
    val commitResult2 = Future.successful(Right(Some(List(commit3))))

    when(store.scmUrls()).thenReturn(Future.successful(scmUrls))
    when(search.parse(url1)).thenReturn(Right(("github", "zalando", "kontrolletti")))
    when(search.parse(url2)).thenReturn(Right(("github", "zalando", "lizzy")))
    when(search.commits("github", "zalando", "kontrolletti", None, None)).thenReturn(commitResult1)
    when(search.commits("github", "zalando", "lizzy", None, None)).thenReturn(commitResult2)
    when(store.saveCommits(List(commit1, commit2))).thenReturn(Future.successful(true))
    when(store.saveCommits(List(commit3))).thenReturn(Future.successful(true))

    Await.result(synchronizer.synchCommits(), Duration("5 seconds")) match {
      case result: List[Future[Boolean]] => result.map {
        Await.result(_, Duration("5 seconds")) match {
          case value: Boolean => assert(value == true)
        }
      }
    }

    verify(store, times(1)).scmUrls()
    verify(search, times(1)).parse(url1)
    verify(search, times(1)).parse(url2)
    verify(search, times(1)).commits("github", "zalando", "kontrolletti", None, None)
    verify(search, times(1)).commits("github", "zalando", "lizzy", None, None)
    verify(store, times(1)).saveCommits(List(commit1, commit2))
    verify(store, times(1)).saveCommits(List(commit3))
  }

}