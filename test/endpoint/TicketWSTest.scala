package endpoint

import scala.{ Left, Right }
import scala.concurrent.Future
import org.mockito.Mockito.{ reset, times, verify, when }
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import client.RequestDispatcher
import client.oauth.OAuth
import configuration.OAuthConfiguration
import dao.{ CommitRepository, FilterParameters, PageParameters, PagedResult, RepoParameters }
import model.KontrollettiToJsonParser.ticketWriter
import model.Ticket
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule.fromPlayBinding
import play.api.test.FakeRequest
import play.api.test.Helpers.{ GET, INTERNAL_SERVER_ERROR, NOT_FOUND, OK, contentAsString, contentType, defaultAwaitTimeout, header, route, status, writeableOf_AnyContentAsEmpty }
import play.api.libs.json.Json
import scala.{ Left, Right }
import scala.concurrent.Future
import org.mockito.Mockito.{ reset, times, verify, when }
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import client.RequestDispatcher
import client.oauth.OAuth
import configuration.OAuthConfiguration
import model.KontrollettiToJsonParser.ticketWriter
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule.fromPlayBinding
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{ GET, INTERNAL_SERVER_ERROR, NOT_FOUND, OK, contentAsString, contentType, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty }
import service.Search
import dao.CommitRepository
import dao.PagedResult
import model.Ticket
import test.util.{OAuthTestBuilder,KontrollettiOneAppPerTestWithOverrides,MockitoUtils}

class TicketWSTest extends PlaySpec with MockitoSugar with MockitoUtils with KontrollettiOneAppPerTestWithOverrides with OAuthTestBuilder with BeforeAndAfter {
  val reposRoute = "/api/repos/"
  val host = "github.com"
  val project = "zalando"
  val repositoryName = "kontrolletti"
  val sinceId = Some("sinceId")
  val untilId = Some("untilId")
  val repo = new RepoParameters(host, project, repositoryName)
  val filter = new FilterParameters(sinceId, untilId, None)
  val page = new PageParameters(None, None)

  val commitRepository = mock[CommitRepository]

  before {
    reset(commitRepository)
    recordOAuthPassAuthenticationBehaviour
  }

  after {
    verifyOAuthPassAuthenticationBehaviour
  }

  def ticketRoute(host: String = host, project: String = project, repository: String = repositoryName, sinceId: Option[String], untilId: Option[String]) = {
    val since = sinceId.getOrElse("default")
    val until = untilId.getOrElse("default")
    s"/api/hosts/$host/projects/$project/repos/$repository/tickets?since=$since&until=$until"
  }

  "GET /api/hosts/{host}/projects/{project}/repos/{repository}/tickets" should {
    "Return 200 when tickets are found" in {
      val url = ticketRoute(sinceId = sinceId, untilId = untilId)
      val ticket = createTicket("name", "description", "href", Nil)
      val tickets = List(ticket)
      val commits = List(createCommit(tickets = Option(tickets)))
      val commitResult = Future.successful(commits)
      val ticketResult = Future.successful(new PagedResult(tickets.toSeq, 1))

      when(commitRepository.tickets(repo, filter, page)).thenReturn(ticketResult)

      val Some(result) = route(FakeRequest(GET, url).withHeaders(authorizationHeader))
      status(result) mustEqual OK
      contentType(result) mustEqual Some("application/x.zalando.ticket+json")
      header(X_TOTAL_COUNT, result) mustBe Some("1")
      contentAsString(result) mustEqual Json.stringify(Json.toJson(List(ticket)))

      verify(commitRepository, times(1)).tickets(repo, filter, page)
    }

    "Return 404 when the result is empty" in {
      val url = ticketRoute(sinceId = sinceId, untilId = untilId)
      val ticketResult = Future.successful(Right(None))

      when(commitRepository.tickets(repo, filter, page)).thenReturn(Future.successful(new PagedResult[Ticket](Nil, 0)))

      val Some(result) = route(FakeRequest(GET, url).withHeaders(authorizationHeader))
      status(result) mustEqual NOT_FOUND
      header(X_TOTAL_COUNT, result) mustBe None
      verify(commitRepository, times(1)).tickets(repo, filter, page)
    }

  }
  override def overrideModules = {
    Seq(
      bind[CommitRepository].toInstance(commitRepository), //
      bind[OAuthConfiguration].toInstance(oauthConfig), //
      bind[RequestDispatcher].toInstance(dispatcher), //
      bind[OAuth].toInstance(oauthClient) //
      )
  }
}