package service

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
import test.util.MockitoUtils
import akka.actor.ActorSystem
import akka.actor.Scheduler
import org.joda.time.DateTime
import dao.RepoRepository
import org.mockito.ArgumentCaptor
import model.Repository
import configuration.GeneralConfiguration
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfter
import model.Repository

class ImportRepositoryTest extends FlatSpec with MockitoSugar with MockitoUtils with Matchers with BeforeAndAfter {

  //Mocks
  private val kioClient = mock[KioClient]
  private val oAuthClient = mock[OAuth]
  private val actorSystem = mock[ActorSystem]
  private val scheduler = mock[Scheduler]
  private val repoRepository = mock[RepoRepository]
  private val config = mock[GeneralConfiguration]
  when(actorSystem.scheduler).thenReturn(scheduler)

  val dateToday = new DateTime

  //repositories
  val repoPrefix = "https://github.com/zalando-bus"
  val unvalidRepo = createRepository(url = s"url1", host = "host1", project = "project1", repository = "repository")
  val validRepo1 = createRepository(url = s"$repoPrefix/repo1")
  val validRepo2 = createRepository(url = s"$repoPrefix/repo2")
  val validRepo3 = createRepository(url = s"$repoPrefix/repo3")

  private val repoImporter = new ImportRepositoriesImpl(oAuthClient, kioClient, repoRepository, config)

  before {
    reset(kioClient, oAuthClient, scheduler, repoRepository, config)

  }

  "ImportRepository#syncApps" should "store apps from kio in data-store" in {

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
    when(repoRepository.save(any[List[Repository]]())).thenReturn(Future.successful {})

    Await.result(repoImporter.syncApps(), Duration("500 seconds"))

    val capturedRepos = ArgumentCaptor.forClass(classOf[List[Repository]])
    verify(oAuthClient, times(1)).accessToken()
    verify(kioClient, times(1)).repositories(accessToken)
    verify(repoRepository, times(1)).save(capturedRepos.capture())
    assert(capturedRepos.getValue.find(x => x.url == validRepo1.url) != None)
    assert(capturedRepos.getValue.find(x => x.url == validRepo2.url) != None)
  }

  "ImportRepository#removeDuplicates" should "return Collection without duplicates" in {
    val r1 = new Repository("url1", "host1", "project1", "repository1", true, None, None, None)
    val r2 = new Repository("", "host2", "project2", "repository2", true, None, None, None)
    val r3 = new Repository("", "host3", "project3", "repository3", true, None, None, None)
    val r4 = new Repository("", "host4", "project4", "repository4", true, None, None, None)
    val r5 = new Repository("", "host5", "project5", "repository5", true, None, None, None)
    val d1 = new Repository("", "host1", "project1", "repository1", true, None, None, None)
    val d4 = new Repository("", "host4", "project4", "repository4", true, None, None, None)
    val result = repoImporter.removeDuplicates(List(r1, r2, r3, r4, r5, d1, d4))

    result.size shouldBe 5
    result.contains(r1) shouldBe false
    result.contains(r2) shouldBe true
    result.contains(r3) shouldBe true
    result.contains(r5) shouldBe true
    result.contains(d1) shouldBe true
    result.contains(d4) shouldBe true

  }
  "ImportRepository#notInRightHandFilter" should "return Collection without duplicates" in {
    val r1 = new Repository("url1", "host1", "project1", "repository1", true, None, None, None)
    val r2 = new Repository("", "host2", "project2", "repository2", true, None, None, None)
    val r3 = new Repository("", "host3", "project3", "repository3", true, None, None, None)
    val r4 = new Repository("", "host4", "project4", "repository4", true, None, None, None)
    val r5 = new Repository("", "host5", "project5", "repository5", true, None, None, None)
    val left = List(r1, r2, r3)
    val right = List(r2, r4, r5)
    val result = Await.result(repoImporter.notInRightHandFilter(left, right), Duration("50 seconds"))
    result.size shouldBe 2
    result.contains(r1)
    result.contains(r3)

    Await.result(repoImporter.notInRightHandFilter(left, Nil), Duration("50 seconds")) shouldBe left
    Await.result(repoImporter.notInRightHandFilter(Nil, right), Duration("50 seconds")) shouldBe Nil

  }
  "ImportRepository#reposAreEqual" should "evaluate equality of the repositories" in {
    val r1 = new Repository("url1", "host1", "project1", "repository1", true, None, None, None)
    val r2 = new Repository("url2", "host2", "project2", "repository2", true, None, None, None)
    repoImporter.reposAreEqual(r1, r2) shouldBe false
    repoImporter.reposAreEqual(r2, r2) shouldBe true
    repoImporter.reposAreEqual(r1, r1) shouldBe true

  }
}
