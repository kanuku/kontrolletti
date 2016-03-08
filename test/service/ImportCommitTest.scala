package service

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
import test.util.MockitoUtils
import akka.actor.ActorSystem
import akka.actor.Scheduler
import org.joda.time.DateTime
import dao.CommitRepository
import dao.RepoRepository
import org.scalatest.Ignore
import org.mockito.ArgumentCaptor
import model.Repository
import configuration.GeneralConfiguration
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfter
import model.Commit
import model.Ticket

class ImportCommitTest extends FlatSpec with MockitoSugar with MockitoUtils with Matchers with BeforeAndAfter {

  //Mocks
  private val kioClient = mock[KioClient]
  private val oAuthClient = mock[OAuth]
  private val commitRepo = mock[CommitRepository]
  private val search = mock[Search]
  private val actorSystem = mock[ActorSystem]
  private val scheduler = mock[Scheduler]
  private val repoRepository = mock[RepoRepository]
  private val config = mock[GeneralConfiguration]
  when(actorSystem.scheduler).thenReturn(scheduler)

  val dateToday = new DateTime

  //Tickets variables
  val jira = "https://jira.com"
  val gh = "https://github.com"
  val ghe = "https://github-enterprise.com"

  //repository
  val repoPrefix = "https://github.com/zalando-bus"
  val validRepo1 = createRepository(url = s"$repoPrefix/import-test/repo1", host = "host", project = "project", repository = "repository1")

  val author1 = createAuthor("Author1name", "email", None)
  val commit1Repo1 = createCommit(s"commitRepo1-id1", "message", None, author1, None, None, None, dateToday, validRepo1.url)
  val commit2Repo1 = createCommit(s"commitRepo1-id2", "message", None, author1, None, None, None, dateToday, validRepo1.url)

  private val commitImporter = new ImportCommitImpl(actorSystem, oAuthClient, commitRepo, search, repoRepository, config)

  before {
    reset(kioClient, oAuthClient, commitRepo, search, scheduler, repoRepository, config)
    when(config.ticketReferenceJiraBrowseUrl).thenReturn(jira)
    when(config.ticketReferenceGithubHost).thenReturn(gh)
    when(config.ticketReferenceGithubEnterpriseHost).thenReturn(ghe)

  }

  "Tickets#enrichWithTickets" should "transform commit messages into Tickets" in {
    val host = ghe
    val project = "zalando"
    val repository = "kontrolletti"
    val msg1 = "#31 Tickets can be retrieved!"
    val msg2 = "#91 Live deployment is done!"
    val msg3 = "Live deployment is done!"
    val author = createAuthor()
    val commit1 = createCommit(id = "id1", message = msg1)
    val commit2 = createCommit(id = "id2", message = msg2)
    val commit3 = createCommit(id = "id3", message = msg3)
    val result = commitImporter.enrichWithTickets(host, project, repository, List(commit1, commit2, commit3))
    result.size shouldBe 3
    val Some(commitRes1) = result.find { _.id == "id1" }
    val Some(commitRes2) = result.find { _.id == "id2" }
    val Some(commitRes3) = result.find { _.id == "id3" }
    //Check it tickets field/member
    commitRes1.tickets.isDefined shouldBe true
    commitRes2.tickets.isDefined shouldBe true
    commitRes3.tickets shouldBe None
    //Check validation
    commitRes1.valid shouldBe Option(true)
    commitRes2.valid shouldBe Option(true)
    commitRes3.valid shouldBe Option(false)

    val Some(ticketsCommit1) = commitRes1.tickets
    val Some(ticketsCommit2) = commitRes2.tickets
    ticketsCommit1.size shouldBe 1
    ticketsCommit2.size shouldBe 1
    ticketsCommit1(0) shouldBe new Ticket(msg1, s"$host/zalando/kontrolletti/issues/31", None)
    ticketsCommit2(0) shouldBe new Ticket(msg2, s"$host/zalando/kontrolletti/issues/91", None)
  }

  "Import#jiraTicketUrlt" should "get URL from Jira" in {
    commitImporter.jiraTicketUrl shouldBe jira
  }
  "Import#githubHost" should "get github hostname" in {
    commitImporter.githubHost shouldBe gh
  }
  "Import#enrichTickets" should "transform commits messages into tickets" in {
    commitImporter.githubEnterpriseHost shouldBe ghe
  }
}
