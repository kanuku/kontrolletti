package endpoint

import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.test._
import play.api.test.Helpers._
import test.util.MockitoUtils
import java.net.URLEncoder
import org.scalatest.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import client.scm.SCM
import play.api.Logger
import play.api.inject.bind
import service.Search
import org.mockito.Matchers._
import scala.concurrent.Future
import model.Repository
import test.util.MockitoUtils
import play.api.libs.json.Json
import model.KontrollettiToJsonParser._
import org.scalatest.Ignore
import test.util.KontrollettiOneAppPerTestWithOverrides
import org.scalatest.BeforeAndAfter
import dao.CommitRepository
import dao.RepoRepository

class RepoWSTest extends PlaySpec with KontrollettiOneAppPerTestWithOverrides with MockitoSugar with MockitoUtils with BeforeAndAfter {
  private val X_NORMALIZED_REPOSITORY_URL_HEADER = "X-Normalized-Repository-URL"
  private val alternativeUrl = "git@github.com:zalando/kontrolletti.git"
  private val reposRoute = "/api/repos/"
  private val defaultUrl = "https://github.com/zalando/kontrolletti"
  private val encodedDefaultUrl = URLEncoder.encode(defaultUrl, "UTF-8")
  private val erraneousUrl = "asdfj-com"
  private val encodedAlternativeUrl = URLEncoder.encode(alternativeUrl, "UTF-8");
  private val host = "github.com"
  private val project = "zalando"
  private val repoName = "kontrolletti"
  private val search = mock[Search]
  private val repoRepository = mock[RepoRepository]

  before {
    reset(search,repoRepository)
  }

  "HEAD /api/repos" should {

    "Return 200 when the repository-parameter is normalized and the resource can be found" in {
      val response = Right((host, project, repoName))
      val url = reposRoute + encodedDefaultUrl

      when(search.parse(defaultUrl)).thenReturn(response)
      when(search.normalize(host, project, repoName)).thenReturn(defaultUrl)
      when(search.isRepo(host, project, repoName)).thenReturn(Future.successful(Right(true)))

      val result = route(FakeRequest(HEAD, url)).get
      status(result) mustEqual OK
      header(LOCATION, result) mustBe empty
      header(X_NORMALIZED_REPOSITORY_URL_HEADER, result) === Some(X_NORMALIZED_REPOSITORY_URL_HEADER -> defaultUrl)
      contentAsString(result) mustBe empty

      verify(search, times(1)).parse(defaultUrl)
      verify(search, times(1)).normalize(host, project, repoName)
      verify(search, times(1)).isRepo(host, project, repoName)
    }

    "Return 400 (Bad Request) on invalid(not parsable) URI" in {
      val response = Left("Your input was not parsable")
      val url = reposRoute + erraneousUrl

      when(search.parse(erraneousUrl)).thenReturn(response)
      //Let Guice return mocked searchService

      val result = route(FakeRequest(HEAD, url)).get
      status(result) mustEqual BAD_REQUEST
      header(LOCATION, result) mustBe empty
      contentAsString(result) mustBe empty

      verify(search, times(1)).parse(erraneousUrl)
    }

    "Return 301 (Moved permanently) on a URI that is normalizable and returns 200" in {

      val parsedResponse = Right((host, project, repoName))
      val existsResponse = Future.successful(Right(true))

      when(search.parse(alternativeUrl)).thenReturn(parsedResponse)
      when(search.normalize(host, project, repoName)).thenReturn(s"/projects/$project/repos/$repoName")
      when(search.isRepo(host, project, repoName)).thenReturn(existsResponse)

      val result = route(FakeRequest(HEAD, s"$reposRoute$encodedAlternativeUrl")).get
      status(result) mustEqual MOVED_PERMANENTLY
      header(X_NORMALIZED_REPOSITORY_URL_HEADER, result) === Some(X_NORMALIZED_REPOSITORY_URL_HEADER -> defaultUrl)
      header(LOCATION, result) === Some(LOCATION -> s"$reposRoute$encodedAlternativeUrl")
      contentAsString(result) mustBe empty

      verify(search, times(1)).parse(alternativeUrl)
      verify(search, times(1)).normalize(host, project, repoName)
      verify(search, times(1)).isRepo(host, project, repoName)
    }

    "Return 404 on a URI that is normalized but does not exist" in {

      val parsedResponse = Right((host, project, repoName))
      val existsResponse = Future.successful(Right(false))

      when(search.parse(alternativeUrl)).thenReturn(parsedResponse)
      when(search.normalize(host, project, repoName)).thenReturn(s"/projects/$project/repos/$repoName")
      when(search.isRepo(host, project, repoName)).thenReturn(existsResponse)

      val result = route(FakeRequest(HEAD, s"$reposRoute$encodedAlternativeUrl")).get
      status(result) mustEqual NOT_FOUND
      header(LOCATION, result) mustBe empty

      contentAsString(result) mustBe empty

      verify(search, times(1)).parse(alternativeUrl)
      verify(search, times(1)).normalize(host, project, repoName)
      verify(search, times(1)).isRepo(host, project, repoName)
    }

    "Return 500 on a URI when the Service returns 500" in {
      val parsedResponse = Right((host, project, repoName))
      val existsResponse = Future.successful(Left("Some error happend"))

      when(search.parse(alternativeUrl)).thenReturn(parsedResponse)
      when(search.normalize(host, project, repoName)).thenReturn(s"/projects/$project/repos/$repoName")
      when(search.isRepo(host, project, repoName)).thenReturn(existsResponse)

      val result = route(FakeRequest(HEAD, s"$reposRoute$encodedAlternativeUrl")).get
      status(result) mustEqual INTERNAL_SERVER_ERROR
      header(LOCATION, result) mustBe empty
      contentAsString(result) mustBe empty
      contentType(result) mustEqual Some("application/problem+json")

      verify(search, times(1)).parse(alternativeUrl)
      verify(search, times(1)).normalize(host, project, repoName)
      verify(search, times(1)).isRepo(host, project, repoName)
    }
  }

  "GET /api/repos" should {
    "Return 200 when the Repository exists" in {

      val parsedResponse = Right((host, project, repoName))
      val repository = createRepository()
      val repoResponse = Future.successful(Some(repository))

      when(search.parse(defaultUrl)).thenReturn(parsedResponse)
     when(repoRepository.byParameters(host, project, repoName)).thenReturn(repoResponse)
      val result = route(FakeRequest(GET, s"$reposRoute$encodedDefaultUrl")).get
      status(result) mustEqual OK

      contentAsString(result) mustEqual Json.stringify(Json.toJson(repository))
      contentType(result) mustEqual Some("application/x.zalando.repository+json")

      verify(search, times(1)).parse(defaultUrl)
      verify(repoRepository, times(1)).byParameters(host, project, repoName)
    }

    "Return 400 when called with erraneous url" in {
      val parsedResponse = Left("Some error happend")

      when(search.parse(erraneousUrl)).thenReturn(parsedResponse)

      val result = route(FakeRequest(GET, s"$reposRoute$erraneousUrl")).get
      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual parsedResponse.left.get
      contentType(result) mustEqual Some("application/problem+json")
      verify(search, times(1)).parse(erraneousUrl)
    }

    "Return 404 when it results in a None" in {
      val parsedResponse = Right((host, project, repoName))
      val repoResponse = Future.successful(None)
      when(search.parse(defaultUrl)).thenReturn(parsedResponse)
      when(repoRepository.byParameters(host, project, repoName)).thenReturn(repoResponse)

      val result = route(FakeRequest(GET, s"$reposRoute$encodedDefaultUrl")).get
      status(result) mustEqual NOT_FOUND
      contentAsString(result) mustBe empty

      verify(search, times(1)).parse(defaultUrl)
      verify(repoRepository, times(1)).byParameters(host, project, repoName)
    }

  }
  override def overrideModules = {
    Seq(bind[Search].toInstance(search),
      bind[RepoRepository].toInstance(repoRepository))
  }
}