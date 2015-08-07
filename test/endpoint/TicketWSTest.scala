package endpoint

import scala.concurrent._
import play.api.test.FakeApplication
import org.specs2.mutable._
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
import model.KontrollettiToJsonParser._

class SpecificationWSTest extends PlaySpec with MockitoSugar with MockitoUtils {
  val reposRoute = "/api/repos/"
  val host = "github.com"
  val project = "zalando"
  val repo = "kontrolletti"
  val sinceId = Some("sinceId")
  val untilId = Some("untilId")

  def ticketRoute(host: String = host, project: String = project, repository: String = repo, sinceId: Option[String], untilId: Option[String]) = {
    val since = if (sinceId.isDefined) sinceId.get else None
    val until = if (untilId.isDefined) untilId.get else None
    s"/api/hosts/$host/projects/$project/repos/$repository/tickets?since=$since&until=$until"
  }
   
  "GET /api/hosts/{host}/projects/{project}/repos/{repository}/tickets" should {
    "Return 200 when tickets are found" in {
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
        contentAsString(result) mustEqual Json.stringify(Json.toJson(List(ticket)))
      }

      verify(search, times(1)).tickets(host, project, repo, sinceId, untilId)
    }

    "Return 404 when the result is empty" in {
      val url = ticketRoute(sinceId = sinceId, untilId = untilId)
      val search = mock[Search]
      val ticketResult = Future.successful(Right(None))

      when(search.tickets(host, project, repo, sinceId, untilId)).thenReturn(ticketResult)

      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        val Some(result) = route(FakeRequest(GET, url))
        status(result) mustEqual NOT_FOUND
      }

      verify(search, times(1)).tickets(host, project, repo, sinceId, untilId)
    }

    "Return 500 when the result is an error" in {
      val url = ticketRoute(sinceId = sinceId, untilId = untilId)
      val error = "Some Error"
      val search = mock[Search]
      val ticketResult = Future.successful(Left(error))

      when(search.tickets(host, project, repo, sinceId, untilId)).thenReturn(ticketResult)

      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        val Some(result) = route(FakeRequest(GET, url))
        status(result) mustEqual INTERNAL_SERVER_ERROR
        contentType(result) mustEqual Some("application/problem+json")
      }

      verify(search, times(1)).tickets(host, project, repo, sinceId, untilId)
    }
  }
}