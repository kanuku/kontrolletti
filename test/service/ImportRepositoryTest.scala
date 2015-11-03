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
  val validRepo1 = createRepository(url = s"$repoPrefix/import-test/repo1", host = "host", project = "project", repository = "repository1")
  val validRepo2 = createRepository(url = s"$repoPrefix/import-test/repo2", host = "host", project = "project", repository = "repository2")
  val validRepo3 = createRepository(url = s"$repoPrefix/import-test/repo3", host = "host", project = "project", repository = "repository3")

  private val repoImporter = new ImportRepositoriesImpl(oAuthClient, kioClient, repoRepository, config)

  before {
    reset(kioClient, oAuthClient, scheduler, repoRepository, config)

  }

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
    when(repoRepository.save(any[List[Repository]]())).thenReturn(Future.successful {})

    Await.result(repoImporter.syncApps(), Duration("500 seconds"))

    val capturedRepos = ArgumentCaptor.forClass(classOf[List[Repository]])
    verify(oAuthClient, times(1)).accessToken()
    verify(kioClient, times(1)).repositories(accessToken)
    verify(repoRepository, times(1)).save(capturedRepos.capture())
    assert(capturedRepos.getValue.find(x => x.url == validRepo1.url) != None)
    assert(capturedRepos.getValue.find(x => x.url == validRepo2.url) != None)
  }

}
