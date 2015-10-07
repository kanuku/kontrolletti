package endpoint

import scala.concurrent._
import play.api.test.FakeApplication
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play.PlaySpec
import org.scalatest.mock.MockitoSugar
import test.util.MockitoUtils
import model.Commit
import model.Author
import model.Link
import org.mockito.Matchers._
import org.mockito.Mockito._
import service.Search
import scala.concurrent.Future
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.duration.Duration
import play.api.libs.json.Json
import java.net.URLEncoder
import play.api.inject.bind
import model.KontrollettiToJsonParser._
import org.scalatest.Ignore
import test.util.KontrollettiOneAppPerTestWithOverrides
import org.scalatest.BeforeAndAfter
import test.util.OAuthTestBuilder
import client.RequestDispatcher
import configuration.OAuthConfiguration

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
    recordOAuthBehaviour
  }

  after {
    verifyOAuthBehaviour
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

      val result = route(FakeRequest(GET, url)).get
      status(result) mustEqual OK
      contentType(result) mustEqual Some("application/x.zalando.ticket+json")
      contentAsString(result) mustEqual Json.stringify(Json.toJson(List(ticket)))

      verify(search, times(1)).tickets(host, project, repo, sinceId, untilId)
    }

    "Return 404 when the result is empty" in {
      val url = ticketRoute(sinceId = sinceId, untilId = untilId)
      val ticketResult = Future.successful(Right(None))

      when(search.tickets(host, project, repo, sinceId, untilId)).thenReturn(ticketResult)

      val result = route(FakeRequest(GET, url)).get
      status(result) mustEqual NOT_FOUND

      verify(search, times(1)).tickets(host, project, repo, sinceId, untilId)
    }

    "Return 500 when the result is an error" in {
      val url = ticketRoute(sinceId = sinceId, untilId = untilId)
      val error = "Some Error"
      val ticketResult = Future.successful(Left(error))

      when(search.tickets(host, project, repo, sinceId, untilId)).thenReturn(ticketResult)

      val result = route(FakeRequest(GET, url)).get
      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentType(result) mustEqual Some("application/problem+json")

      verify(search, times(1)).tickets(host, project, repo, sinceId, untilId)
    }
  }
  override def overrideModules = {
    Seq(
      bind[Search].toInstance(search), //
      bind[OAuthConfiguration].toInstance(oauthConfig), //
      bind[RequestDispatcher].toInstance(dispatcher))
  }
}