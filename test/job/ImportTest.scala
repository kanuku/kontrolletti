package job

import scala.Right
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.Mockito.when
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import client.kio.KioClient
import client.oauth.OAuth
import service.ImportImpl
import service.DataStore
import service.Search
import test.util.MockitoUtils
import akka.actor.ActorSystem
import akka.actor.Scheduler
import dao.AppInfoRepository

class ImportTest extends FlatSpec with MockitoSugar with MockitoUtils {

  private val kioClient = mock[KioClient]
  private val oAuthClient = mock[OAuth]
  private val store = mock[DataStore]
  private val search = mock[Search]
  private val actorSystem = mock[ActorSystem]
  private val scheduler = mock[Scheduler]
 private val appRepository = mock[AppInfoRepository]
  when(actorSystem.scheduler).thenReturn(scheduler)

  private val synchronizer = new ImportImpl(oAuthClient, store, kioClient, search,appRepository)

  "Synchronizer#syncApps" should "store apps from kio in data-store" in {

    val accessToken = createOAuthAccessToken("token_type", "access_token", "scope", 3599)
    val accessTokenResult = Future.successful { accessToken }
    val appIds = List("kontrolletti", "cloud-lobster")
    val appIdsResult = Future.successful(appIds)
    val validAppInfo = createAppInfo("https://git-hub.com/zalando-bus/kontrolletti/", Option("specUrl1"), Option("docUrl1"),  Option("lastModified1"))
    val unvalidAppInfo = createAppInfo("scmUrl2", Option("specUrl2"), Option("docUrl2"), Option("lastModified2"))
    val apps = List(validAppInfo, unvalidAppInfo)
    val appsResult = Future.successful(apps)

    when(oAuthClient.accessToken()).thenReturn(accessTokenResult)
    when(kioClient.apps(accessToken)).thenReturn(appsResult)
    

    Await.result(synchronizer.syncApps(), Duration("5 seconds")) 

    verify(oAuthClient, times(1)).accessToken()
    verify(kioClient, times(1)).apps(accessToken)
    verify(appRepository, times(1)).save(List(validAppInfo))
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

    Await.result(synchronizer.synchCommits(), Duration("5 seconds")) 
    verify(store, times(1)).scmUrls()
    verify(search, times(1)).parse(url1)
    verify(search, times(1)).parse(url2)
    verify(search, times(1)).commits("github", "zalando", "kontrolletti", None, None)
    verify(search, times(1)).commits("github", "zalando", "lizzy", None, None)
    verify(store, times(1)).saveCommits(List(commit1, commit2))
    verify(store, times(1)).saveCommits(List(commit3))
  }

}