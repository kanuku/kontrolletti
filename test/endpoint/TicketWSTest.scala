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
import model.KontrollettiToJsonParser.ticketWriter
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule.fromPlayBinding
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{ GET, INTERNAL_SERVER_ERROR, NOT_FOUND, OK, contentAsString, contentType, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty }
import service.Search
import test.util.{ KontrollettiOneAppPerTestWithOverrides, MockitoUtils, OAuthTestBuilder }

class TicketWSTest extends PlaySpec with MockitoSugar with MockitoUtils with KontrollettiOneAppPerTestWithOverrides with OAuthTestBuilder with BeforeAndAfter {
  val reposRoute = "/api/repos/"
  val host = "github.com"
  val project = "zalando"
  val repo = "kontrolletti"
  val sinceId = Some("sinceId")
  val untilId = Some("untilId")
  val search = mock[Search]

  before {
    reset(search)
    recordOAuthPassAuthenticationBehaviour
  }

  after {
    verifyOAuthPassAuthenticationBehaviour
  }

  def ticketRoute(host: String = host, project: String = project, repository: String = repo, sinceId: Option[String], untilId: Option[String]) = {
    val since = sinceId.getOrElse("default")
    val until = untilId.getOrElse("default")
    s"/api/hosts/$host/projects/$project/repos/$repository/tickets?since=$since&until=$until"
  }

  "GET /api/hosts/{host}/projects/{project}/repos/{repository}/tickets" should {
    "Return 200 when tickets are found" in {
      val url = ticketRoute(sinceId = sinceId, untilId = untilId)
      val ticket = createTicket()
      val tickets = List(ticket)
      val ticketResult = Future.successful(Right(Some(tickets)))

      when(search.tickets(host, project, repo, sinceId, untilId)).thenReturn(ticketResult)

      val result = route(FakeRequest(GET, url).withHeaders(authorizationHeader)).get
      status(result) mustEqual OK
      contentType(result) mustEqual Some("application/x.zalando.ticket+json")
      contentAsString(result) mustEqual Json.stringify(Json.toJson(List(ticket)))

      verify(search, times(1)).tickets(host, project, repo, sinceId, untilId)
    }

    "Return 404 when the result is empty" in {
      val url = ticketRoute(sinceId = sinceId, untilId = untilId)
      val ticketResult = Future.successful(Right(None))

      when(search.tickets(host, project, repo, sinceId, untilId)).thenReturn(ticketResult)

      val result = route(FakeRequest(GET, url).withHeaders(authorizationHeader)).get
      status(result) mustEqual NOT_FOUND

      verify(search, times(1)).tickets(host, project, repo, sinceId, untilId)
    }

    "Return 500 when the result is an error" in {
      val url = ticketRoute(sinceId = sinceId, untilId = untilId)
      val error = "Some Error"
      val ticketResult = Future.successful(Left(error))

      when(search.tickets(host, project, repo, sinceId, untilId)).thenReturn(ticketResult)

      val result = route(FakeRequest(GET, url).withHeaders(authorizationHeader)).get
      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentType(result) mustEqual Some("application/problem+json")

      verify(search, times(1)).tickets(host, project, repo, sinceId, untilId)
    }
  }
  override def overrideModules = {
    Seq(
      bind[Search].toInstance(search), //
      bind[OAuthConfiguration].toInstance(oauthConfig), //
      bind[RequestDispatcher].toInstance(dispatcher), //
      bind[OAuth].toInstance(oauthClient) //
      )
  }
}