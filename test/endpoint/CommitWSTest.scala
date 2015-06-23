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

class CommitWSTest extends PlaySpec with MockitoSugar with MockitoUtils {
  val host = "github.com"
  val project = "zalando"
  val repo = "kontrolletti"
  def diffRoute(host: String = host, project: String = project, //
                repository: String = repo, source: String, target: String) = s"/api/hosts/$host/projects/$project/repos/$repository/diff/$source...$target"

  "GET /api/hosts/{host}/projects/{project}/repos/{repository}/diff/{source}...{target}" should {
    "Redirect (303) to the right scm GUI " in {
      val source = "commitId1"
      val target = "commitId2"
      val url = diffRoute(source = source, target = target)
      val link = Link("http://link", "", "", "")
      val diffExistsResult = Future.successful(Right(Some(link)))

      val search = mock[Search]

      when(search.diffExists(host, project, repo, source, target)).thenReturn(diffExistsResult)

      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        val Some(result) = route(FakeRequest(GET, url))
        status(result) mustEqual SEE_OTHER
        header(LOCATION, result).get === (LOCATION -> URLEncoder.encode(link.href, "UTF-8"))
        contentAsString(result) mustBe empty
      }

      verify(search, times(1)).diffExists(host, project, repo, source, target)

    }
    "Return 404 if the diff cannot be found" in {
      val source = "commitId1"
      val target = "commitId2"
      val url = diffRoute(source = source, target = target)
      val link = Right(None)
      val diffExistsResult = Future.successful(link)

      val search = mock[Search]

      when(search.diffExists(host, project, repo, source, target)).thenReturn(diffExistsResult)

      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        val Some(result) = route(FakeRequest(GET, url))
        status(result) mustEqual NOT_FOUND
        header(LOCATION, result) mustBe empty
        contentAsString(result) mustBe empty
      }
      verify(search, times(1)).diffExists(host, project, repo, source, target)
    }
    "Return 500 if the (301) to the right scm GUI " in {
      val source = "commitId1"
      val target = "commitId2"
      val url = diffRoute(source = source, target = target)
      val link = Left("error")
      val diffExistsResult = Future.successful(link)

      val search = mock[Search]

      when(search.diffExists(host, project, repo, source, target)).thenReturn(diffExistsResult)

      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        val Some(result) = route(FakeRequest(GET, url))
        status(result) mustEqual INTERNAL_SERVER_ERROR
        header(LOCATION, result) mustBe empty
        contentAsString(result) mustBe empty
      }
      verify(search, times(1)).diffExists(host, project, repo, source, target)
    }

  }

}