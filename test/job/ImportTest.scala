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
import service.Search
import test.util.MockitoUtils
import akka.actor.ActorSystem
import akka.actor.Scheduler
import org.joda.time.DateTime
import dao.CommitRepository
import dao.RepoRepository
import org.scalatest.Ignore

@Ignore
class ImportTest extends FlatSpec with MockitoSugar with MockitoUtils {
//
//  private val kioClient = mock[KioClient]
//  private val oAuthClient = mock[OAuth]
//  private val commitRepo = mock[CommitRepository]
//  private val search = mock[Search]
//  private val actorSystem = mock[ActorSystem]
//  private val scheduler = mock[Scheduler]
//  private val repoRepository = mock[RepoRepository]
//  when(actorSystem.scheduler).thenReturn(scheduler)
//
//  private val synchronizer = new ImportImpl(oAuthClient, commitRepo, kioClient, search, repoRepository)
//
//  "Import#syncApps" should "store apps from kio in data-store" in {
//
//    val accessToken = createOAuthAccessToken("token_type", "access_token", "scope", 3599)
//    val accessTokenResult = Future.successful { accessToken }
//    val appIds = List("kontrolletti", "cloud-lobster")
//    val appIdsResult = Future.successful(appIds)
//    val unvalidAppInfo = createAppInfo("scmUrl2", Option("specUrl2"), Option("docUrl2"), new DateTime)
//    val validAppInfo = createAppInfo("https://git-hub.com/zalando-bus/kontrolletti/", Option("specUrl1"), Option("docUrl1"), new DateTime)
//    val apps = List(validAppInfo, unvalidAppInfo)
//    val appsResult = Future.successful(apps)
//
//    when(oAuthClient.accessToken()).thenReturn(accessTokenResult)
//    when(kioClient.apps(accessToken)).thenReturn(appsResult)
//
//    Await.result(synchronizer.syncApps(), Duration("5 seconds"))
//
//    verify(oAuthClient, times(1)).accessToken()
//    verify(kioClient, times(1)).apps(accessToken)
//    verify(repoRepository, times(1)).save(List(validAppInfo))
//  }
//
//  "Import#synchCommits" should "store apps from kio in data-store" in {
//    
//    val url1 = "https://github.com/zalando/kontrolletti/"
//    val url2 = "https://git-hub.com/zalando-bus/lizzy/"
//    val scmUrls = List(url1, url2)
//    val commit1 = createCommit()
//    val commit2 = createCommit(id = "id2")
//    val commit3 = createCommit(id = "id3")
//    val commitResult1 = Future.successful(Right(Some(List(commit1, commit2))))
//    val commitResult2 = Future.successful(Right(Some(List(commit3))))
//    val emptyResult= Future.successful(Right(Some(List())))
//    val emptyFuture= Future.successful{}
//
//    when(repoRepository.scmUrls()).thenReturn(Future.successful(scmUrls))
//    when(search.parse(url1)).thenReturn(Right(("github", "zalando", "kontrolletti")))
//    when(search.parse(url2)).thenReturn(Right(("github", "zalando", "lizzy")))
//    when(search.commits("github", "zalando", "kontrolletti", None, None,1)).thenReturn(commitResult1)
//    when(search.commits("github", "zalando", "kontrolletti", None, None,2)).thenReturn(emptyResult)
//    when(search.commits("github", "zalando", "lizzy", None, None,1)).thenReturn(commitResult2)
//    when(search.commits("github", "zalando", "lizzy", None, None,2)).thenReturn(emptyResult)
//    when(commitRepo.save(List(commit1, commit2))).thenReturn(emptyFuture)
//    when(commitRepo.save(List(commit3))).thenReturn(emptyFuture)
//
//    Await.result(synchronizer.synchCommits(), Duration("5 seconds"))
//    
//    verify(repoRepository, times(1)).scmUrls()
//    verify(search, times(1)).parse(url1)
//    verify(search, times(1)).parse(url2)
//    verify(search, times(1)).commits("github", "zalando", "kontrolletti", None, None,1)
//    verify(search, times(1)).commits("github", "zalando", "kontrolletti", None, None,2)
//    verify(search, times(1)).commits("github", "zalando", "lizzy", None, None,1)
//    verify(search, times(1)).commits("github", "zalando", "lizzy", None, None,2)
//    verify(commitRepo, times(1)).save(List(commit1, commit2))
//    verify(commitRepo, times(1)).save(List(commit3))
//  }
//
//  "Import#updateChildIds" should "update childId parameters" in {
//    val commit1 = createCommit(id = "id1", parentIds = List("id2", "id3"))
//    val commit2 = createCommit(id = "id2",parentIds = List("id5"))
//    val commit3 = createCommit(id = "id3",parentIds = List("id7"))
//    val commits= List(commit1, commit2, commit3)
//    val result = synchronizer.updateChildIds(commits)
//    assert(result.size==3, "Size should be, none element should be removed!!")
//    println(commits)
//    println(result)
//    val Some(foundCommit1)=result.find { _.id == "id1" }
//    val Some(foundCommit2)=result.find { _.id == "id2" }
//    val Some(foundCommit3)=result.find { _.id == "id3" }
//    assert(foundCommit1.id == "id1")
//    assert(foundCommit1.childId == None)
//    assert(foundCommit2.id == "id2")
//    assert(foundCommit2.childId == Some("id1"))
//    assert(foundCommit3.id == "id3")
//    assert(foundCommit3.childId == Some("id1"))
//
//  }

}