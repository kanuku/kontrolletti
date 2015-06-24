package endpoint

import scala.concurrent._
import play.api.test.FakeApplication
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
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

class TicketWSTest extends PlaySpec with MockitoSugar with MockitoUtils {
  val NORMALIZED_REQUEST_PARAMETER = "X-Normalized-Repository-Identifier"
  val reposRoute = "/api/repos/"
  val defaultUrl = "https://github.com/zalando/kontrolletti"
  val host = "github.com"
  val project = "zalando"

  def ticketRoute(host: String = host, project: String = project, //
                  repository: String = repo, sinceId: String, untilId: String) = s"/api/hosts/$host/projects/$project/repos/$repository/tickets"

  val repo = "kontrolletti"
  "HEAD /api/repos" should {

    "Return 200 when tickets are found" in {

      val sinceId = "sinceId"
      val untilId = "untilId"
      val url = ticketRoute(sinceId = sinceId, untilId = untilId)
      val ticket = createTicket()
      val tickets = List(ticket)
      val search = mock[Search]
      val ticketResult = Future.successful(Right(Some(tickets)))

      when(search.tickets(host, project, repo, sinceId, untilId)).thenReturn(ticketResult)

      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        val Some(result) = route(FakeRequest(GET, url))
        status(result) mustEqual OK
        contentType(result) mustEqual Some("application/x.zalando.ticket+json")
        import model.KontrollettiToModelParser._
        contentAsString(result) mustEqual Json.stringify(Json.toJson(ticket))
      }

      verify(search, times(1)).tickets(host, project, repo, sinceId, untilId)
    }
    "Return 404 when the result is empty" in {

      val sinceId = "sinceId"
      val untilId = "untilId"
      val url = ticketRoute(sinceId = sinceId, untilId = untilId)
      val tickets = List()
      val search = mock[Search]
      val ticketResult = Future.successful(Right(Some(tickets)))

      when(search.tickets(host, project, repo, sinceId, untilId)).thenReturn(ticketResult)

      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        val Some(result) = route(FakeRequest(GET, url))
        status(result) mustEqual OK
        contentType(result) mustEqual Some("application/x.zalando.ticket+json")
      }

      verify(search, times(1)).tickets(host, project, repo, sinceId, untilId)
    }
    "Return 500 when the result is an error" in {

      val sinceId = "sinceId"
      val untilId = "untilId"
      val url = ticketRoute(sinceId = sinceId, untilId = untilId)
      val error = "Some Error"
      val search = mock[Search]
      val ticketResult = Future.successful(Left(error))

      when(search.tickets(host, project, repo, sinceId, untilId)).thenReturn(ticketResult)

      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        val Some(result) = route(FakeRequest(GET, url))
        status(result) mustEqual OK
        contentType(result) mustEqual Some("application/x.zalando.ticket+json")
      }

      verify(search, times(1)).tickets(host, project, repo, sinceId, untilId)
    }

  }

}