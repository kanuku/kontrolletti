package endpoint

import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.test._
import play.api.test.Helpers._
import test.util.MockitoUtils
import java.net.URLEncoder
import org.specs2.matcher.MustExpectable
import org.scalatest.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import client.SCM
import service.SearchImpl
import play.api.Logger
import service.Search
import com.google.inject.AbstractModule
import com.google.inject.Guice
import org.mockito.Matchers._
import scala.concurrent.Future
import model.Repository
import test.util.MockitoUtils
import play.api.libs.json.Json

class RepoWSTest extends PlaySpec with MockitoSugar with MockitoUtils {

  val reposRoute = "/api/repos/"
  val defaultUrl = "https://github.com/zalando/kontrolletti"
  val encodedDefaultUrl = URLEncoder.encode(defaultUrl, "UTF-8")
  val alternativeUrl = "git@github.com:zalando/kontrolletti.git"
  val encodedAlternativeUrl = URLEncoder.encode(alternativeUrl, "UTF-8");
  val erraneousUrl = "asdfj-com"
  val host = "github.com"
  val project = "zalando"
  val repoName = "kontrolletti"
  "HEAD /api/repos" should {

    "Return 400 (Bad Request) on invalid(not parsable) URI" in {
      val search = mock[Search]
      val response = Left("Your input was not parsable")
      val url = reposRoute + erraneousUrl

      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        when(search.parse(anyString)).thenReturn(response)
        //Let Guice return mocked searchService

        val Some(result) = route(FakeRequest(HEAD, url))
        status(result) mustEqual BAD_REQUEST
        header(LOCATION, result) mustBe empty
        contentAsString(result) mustBe empty
      }

      verify(search, times(1)).parse(erraneousUrl)
    }

    "Return 301 (Moved permanently) on a URI that is normalizable and returns 200" in {

      val parsedResponse = Right((host, project, repoName))
      val existsResponse = Future.successful(Right(true))
      val search = mock[Search]

      when(search.parse(alternativeUrl)).thenReturn(parsedResponse)
      when(search.normalize(host, project, repoName)).thenReturn(s"/projects/$project/repos/$repoName")
      when(search.repoExists(host, project, repoName)).thenReturn(existsResponse)

      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        val Some(result) = route(FakeRequest(HEAD, s"$reposRoute$encodedAlternativeUrl"))
        status(result) mustEqual MOVED_PERMANENTLY
        header(LOCATION, result).get === (LOCATION -> reposRoute + encodedDefaultUrl)
        contentAsString(result) mustBe empty
      }

      verify(search, times(1)).parse(alternativeUrl)
      verify(search, times(1)).normalize(host, project, repoName)
      verify(search, times(1)).repoExists(host, project, repoName)
    }

    "Return 404 on a URI that is normalized but does not exist" in {

      val parsedResponse = Right((host, project, repoName))
      val existsResponse = Future.successful(Right(false))
      val search = mock[Search]

      when(search.parse(alternativeUrl)).thenReturn(parsedResponse)
      when(search.normalize(host, project, repoName)).thenReturn(s"/projects/$project/repos/$repoName")
      when(search.repoExists(host, project, repoName)).thenReturn(existsResponse)

      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        val Some(result) = route(FakeRequest(HEAD, s"$reposRoute$encodedAlternativeUrl"))
        status(result) mustEqual NOT_FOUND
        header(LOCATION, result) mustBe empty

        contentAsString(result) mustBe empty
      }

      verify(search, times(1)).parse(alternativeUrl)
      verify(search, times(1)).normalize(host, project, repoName)
      verify(search, times(1)).repoExists(host, project, repoName)
    }

    "Return 500 on a URI when the Service returns 500" in {
      val parsedResponse = Right((host, project, repoName))
      val existsResponse = Future.successful(Left("Some error happend"))
      val search = mock[Search]

      when(search.parse(alternativeUrl)).thenReturn(parsedResponse)
      when(search.normalize(host, project, repoName)).thenReturn(s"/projects/$project/repos/$repoName")
      when(search.repoExists(host, project, repoName)).thenReturn(existsResponse)

      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        val Some(result) = route(FakeRequest(HEAD, s"$reposRoute$encodedAlternativeUrl"))
        status(result) mustEqual INTERNAL_SERVER_ERROR
        header(LOCATION, result) mustBe empty

        contentAsString(result) mustBe empty
      }

      verify(search, times(1)).parse(alternativeUrl)
      verify(search, times(1)).normalize(host, project, repoName)
      verify(search, times(1)).repoExists(host, project, repoName)
    }
  }

  "GET /api/repos" should {
    "Return 200 when the Repository exists" in {

      val parsedResponse = Right((host, project, repoName))
      val repository = createRepository()
      val repoResponse = Future.successful(Right(List(repository)))
      val search = mock[Search]

      when(search.parse(defaultUrl)).thenReturn(parsedResponse)
      when(search.repos(host, project, repoName)).thenReturn(repoResponse)

      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        val Some(result) = route(FakeRequest(GET, s"$reposRoute$encodedDefaultUrl"))
        status(result) mustEqual OK
        import model.KontrollettiToModelParser._
        contentAsString(result) mustEqual Json.stringify(Json.toJson(repository))
         contentType(result) mustEqual Some("application/x.zalando.repository+json")
      }

      verify(search, times(1)).parse(defaultUrl)
      verify(search, times(1)).repos(host, project, repoName)
    }

    "Return 400 when called with erraneous url" in {
      val search = mock[Search]
      val parsedResponse = Left("Some error happend")

      when(search.parse(erraneousUrl)).thenReturn(parsedResponse)

      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        val Some(result) = route(FakeRequest(GET, s"$reposRoute$erraneousUrl"))
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual parsedResponse.left.get
      }
      verify(search, times(1)).parse(defaultUrl)
    }

    "Return 404 when it results in a empty list" in {
      val search = mock[Search]
      val parsedResponse = Right((host, project, repoName))
      val repoResponse = Future.successful(Right(List()))
      when(search.parse(defaultUrl)).thenReturn(parsedResponse)
      when(search.repos(host, project, repoName)).thenReturn(repoResponse)

      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        val Some(result) = route(FakeRequest(GET, s"$reposRoute$encodedDefaultUrl"))
        status(result) mustEqual NO_CONTENT
        contentAsString(result) mustBe empty
      }
      verify(search, times(1)).parse(defaultUrl)
    }

    "Return 500 when it results in an error" in {
      val search = mock[Search]
      val parsedResponse = Right((host, project, repoName))
      val repoResponse = Future.successful(Right(List()))
      when(search.parse(defaultUrl)).thenReturn(parsedResponse)
      when(search.repos(host, project, repoName)).thenReturn(repoResponse)

      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        val Some(result) = route(FakeRequest(GET, s"$reposRoute$encodedDefaultUrl"))
        status(result) mustEqual INTERNAL_SERVER_ERROR
        contentAsString(result) mustEqual parsedResponse.left.get
      }
      verify(search, times(1)).parse(defaultUrl)
    }
  }
}