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
import test.util.DBModels
import akka.actor.ActorSystem
import akka.actor.Scheduler
import org.joda.time.DateTime
import dao.CommitRepository
import dao.RepoRepository
import org.scalatest.Ignore
import org.mockito.ArgumentCaptor
import model.Repository

class ImportTest extends FlatSpec with MockitoSugar with MockitoUtils {

  //Mocks
  private val kioClient = mock[KioClient]
  private val oAuthClient = mock[OAuth]
  private val commitRepo = mock[CommitRepository]
  private val search = mock[Search]
  private val actorSystem = mock[ActorSystem]
  private val scheduler = mock[Scheduler]
  private val repoRepository = mock[RepoRepository]
  when(actorSystem.scheduler).thenReturn(scheduler)
  
  val dateToday = new DateTime

  //repositories
  val repoPrefix = "https://github.com/zalando-bus"
  val unvalidRepo = createRepository(url = s"url1", host = "host1", project = "project1", repository = "repository")
  val validRepo1 = createRepository(url = s"$repoPrefix/import-test/repo1", host = "host", project = "project", repository = "repository1")
  val validRepo2 = createRepository(url = s"$repoPrefix/import-test/repo2", host = "host", project = "project", repository = "repository2")
  val validRepo3 = createRepository(url = s"$repoPrefix/import-test/repo3", host = "host", project = "project", repository = "repository3")
  
  val author1 = createAuthor("Author1name", "email", None)
  val commit1Repo1 = createCommit(s"commitRepo1-id1", "message", None, author1, None, None, None, None, dateToday, validRepo1.url)
  val commit2Repo1 = createCommit(s"commitRepo1-id2", "message", None, author1, None, None, None, None, dateToday, validRepo1.url)
  
  private val synchronizer = new ImportImpl(oAuthClient, commitRepo, kioClient, search, repoRepository)

  "Import#syncApps" should "store apps from kio in data-store" in {

    val accessToken = createOAuthAccessToken("token_type", "access_token", "scope", 3599)
    val accessTokenResult = Future.successful { accessToken }
    val appIds = List("kontrolletti", "cloud-lobster")
    val appIdsResult = Future.successful(appIds)

    val reposToSave = List(validRepo1, validRepo2)
    val emptySavedRepos = Future.successful(List())
    val savedRepos = Future.successful(List(validRepo3))
    val reposReturnedByKio = Future.successful(reposToSave ++ List(unvalidRepo, validRepo3))

    when(oAuthClient.accessToken()).thenReturn(accessTokenResult)
    when(kioClient.repositories(accessToken)).thenReturn(reposReturnedByKio)
    when(repoRepository.all()).thenReturn(savedRepos)
    when(repoRepository.save(anyObject())).thenReturn(Future.successful {})

    Await.result(synchronizer.syncApps(), Duration("500 seconds"))

    val capturedRepos = ArgumentCaptor.forClass(classOf[List[Repository]])
    verify(oAuthClient, times(1)).accessToken()
    verify(kioClient, times(1)).repositories(accessToken)
    verify(repoRepository, times(1)).save(capturedRepos.capture())
    assert(capturedRepos.getValue.find(x => x.url == validRepo1.url) != None)
    assert(capturedRepos.getValue.find(x => x.url == validRepo2.url) != None)
  }

//  "Import#synchCommits" should "store apps from kio in data-store" in {
//
//    val savedRepos = List(validRepo1, validRepo2)
//
//    when(repoRepository.enabled()).thenReturn(Future.successful(savedRepos))
//    when(search.commits("github.com", "zalando", "kontrolletti", None, None, 1)).thenReturn(commitResult1)
//    when(search.commits("github.com", "zalando", "kontrolletti", None, None, 2)).thenReturn(emptyResult)
//    when(commitRepo.save(List(commit1, commit2))).thenReturn(emptyFuture)
//    when(commitRepo.save(List(commit3))).thenReturn(emptyFuture)
//
//    Await.result(synchronizer.synchCommits(), Duration("5 seconds"))
//
//    verify(repoRepository, times(1)).scmUrls()
//    verify(search, times(1)).parse(url1)
//    verify(search, times(1)).parse(url2)
//    verify(search, times(1)).commits("github", "zalando", "kontrolletti", None, None, 1)
//    verify(search, times(1)).commits("github", "zalando", "kontrolletti", None, None, 2)
//    verify(search, times(1)).commits("github", "zalando", "lizzy", None, None, 1)
//    verify(search, times(1)).commits("github", "zalando", "lizzy", None, None, 2)
//    verify(commitRepo, times(1)).save(List(commit1, commit2))
//    verify(commitRepo, times(1)).save(List(commit3))
//  }
  //
  //  "Import#updateChildIds" should "update childId parameters" in {
  //    val commit1 = createCommit(id = "id1", parentIds = List("id2", "id3"))
  //    val commit2 = createCommit(id = "id2", parentIds = List("id5"))
  //    val commit3 = createCommit(id = "id3", parentIds = List("id7"))
  //    val commits = List(commit1, commit2, commit3)
  //    val result = synchronizer.updateChildIds(commits)
  //    assert(result.size == 3, "Size should be, none element should be removed!!")
  //    println(commits)
  //    println(result)
  //    val Some(foundCommit1) = result.find { _.id == "id1" }
  //    val Some(foundCommit2) = result.find { _.id == "id2" }
  //    val Some(foundCommit3) = result.find { _.id == "id3" }
  //    assert(foundCommit1.id == "id1")
  //    assert(foundCommit1.childId == None)
  //    assert(foundCommit2.id == "id2")
  //    assert(foundCommit2.childId == Some("id1"))
  //    assert(foundCommit3.id == "id3")
  //    assert(foundCommit3.childId == Some("id1"))
  //
  //  }

}