package endpoint

import java.net.URLEncoder
import scala.concurrent._
import scala.concurrent.Future
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.PlaySpec
import model.Commit
import model.CommitsResult
import model.Link
import model.Error
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.json._
import service.Search
import test.util.MockitoUtils
import model.KontrollettiToJsonParser._
import model.CommitResult
import org.scalatest.Ignore
@Ignore
class CommitWSTest extends PlaySpec with OneAppPerSuite with MockitoSugar with MockitoUtils {
  val host = "github.com"
  val project = "zalando"
  val repository = "kontrolletti"
  val sinceId = Some("sinceId")
  val untilId = Some("untilId")

  def diffRoute(host: String = host, project: String = project, repository: String = repository, source: String, target: String) = s"/api/hosts/$host/projects/$project/repos/$repository/diff/$source...$target"
  def commitsRoute(host: String = host, project: String = project, repository: String = repository, sinceId: Option[String] = None, untilId: Option[String] = None) = {
    val since = sinceId.getOrElse("default")
    val until = untilId.getOrElse("deafult")
    s"/api/hosts/github.com/projects/zalando/repos/kontrolletti/commits?since=$since&until=$until"
  }
  def singleCommitRoute(host: String = host, project: String = project, repository: String = repository, commitId: String = "commitId") = {

    s"/api/hosts/$host/projects/$project/repos/$repository/commits/$commitId"
  }

  /**
   * Tests for Diffs starts here
   */
  
    "GET /api/hosts/{host}/projects/{project}/repos/{repository}/diff/{source}...{target}" should {
      "Redirect (303) to the right scm GUI " in {
        val source = "commitId1"
        val target = "commitId2"
        val url = diffRoute(source = source, target = target)
        val link = Link("http://link", "", "", "")
        val diffExistsResult = Future.successful(Right(Some(link)))

        val search = mock[Search]

        when(search.diff(host, project, repository, source, target)).thenReturn(diffExistsResult)

        withFakeApplication(new FakeGlobalWithSearchService(search)) {
          val result = route(FakeRequest(GET, url)).get
          status(result) mustEqual SEE_OTHER
          header(LOCATION, result) === Some(LOCATION -> URLEncoder.encode(link.href, "UTF-8"))
          contentAsString(result) mustBe empty
        }

        verify(search, times(1)).diff(host, project, repository, source, target)

      }

      "Return 404 if the diff cannot be found" in {
        val source = "commitId1"
        val target = "commitId2"
        val url = diffRoute(source = source, target = target)
        val link = Right(None)
        val diffExistsResult = Future.successful(link)

        val search = mock[Search]

        when(search.diff(host, project, repository, source, target)).thenReturn(diffExistsResult)

        withFakeApplication(new FakeGlobalWithSearchService(search)) {
          val result = route(FakeRequest(GET, url)).get
          status(result) mustEqual NOT_FOUND
          header(LOCATION, result) mustBe empty
          contentAsString(result) mustBe empty
        }
        verify(search, times(1)).diff(host, project, repository, source, target)
      }

      "Return 500 if the (301) to the right scm GUI " in {
        val source = "commitId1"
        val target = "commitId2"
        val url = diffRoute(source = source, target = target)
        val link = Left("error")
        val diffExistsResult = Future.successful(link)

        val search = mock[Search]

        when(search.diff(host, project, repository, source, target)).thenReturn(diffExistsResult)

        withFakeApplication(new FakeGlobalWithSearchService(search)) {
          val result = route(FakeRequest(GET, url)).get
          status(result) mustEqual INTERNAL_SERVER_ERROR
          header(LOCATION, result) mustBe empty
          contentAsString(result) mustBe empty
          contentType(result) mustEqual Some("application/problem+json")
        }
        verify(search, times(1)).diff(host, project, repository, source, target)
      }
    }

    /**
     * Tests for Commits starts here
     */
    "GET /api/hosts/{host}/projects/{project}/repos/{repository}/commits" should {
      "Return 200 when objects are found" in {
        val search = mock[Search]
        val commit = createCommit()
        val commits = List(commit)
        val response = new CommitsResult(List(), commits)
        val commitResult = Future.successful(Right(Some(commits)))
        val url = commitsRoute(sinceId = sinceId, untilId = untilId)
        when(search.commits(host, project, repository, None, None)).thenReturn(commitResult)
        withFakeApplication(new FakeGlobalWithSearchService(search)) {
          val result = route(FakeRequest(GET, url)).get
          status(result) mustEqual OK
          contentType(result) mustEqual Some("application/x.zalando.commit+json")
          contentAsString(result) mustEqual Json.stringify(Json.toJson(response))
        }
      }

      "Return 404 when objects are not found" in {
        val search = mock[Search]
        val commitResult = Future.successful(Right(None))
        val url = commitsRoute(sinceId = sinceId, untilId = untilId)
        when(search.commits(host, project, repository, None, None)).thenReturn(commitResult)
        withFakeApplication(new FakeGlobalWithSearchService(search)) {
          val Some(result) = route(FakeRequest(GET, url))
          status(result) mustEqual NOT_FOUND
        }
      }

      "Return 500 when an error occurs" in {
        val search = mock[Search]
        val response = new Error("An error occurred, please check the logs", 500, "undefined")
        val commitResult = Future.successful(Left("error"))
        val url = commitsRoute(sinceId = sinceId, untilId = untilId)
        when(search.commits(host, project, repository, None, None)).thenReturn(commitResult)
        withFakeApplication(new FakeGlobalWithSearchService(search)) {
          val result = route(FakeRequest(GET, url)).get
          status(result) mustEqual INTERNAL_SERVER_ERROR
          contentType(result) mustEqual Some("application/problem+json")
          contentAsString(result) mustEqual Json.stringify(Json.toJson(response))
        }
      }
    }
    "GET /api/hosts/{host}/projects/{project}/repos/{repository}/commits/{id}" should {
      "Return 200 when the object is found" in {
        val search = mock[Search]
        val commitId = "commitId"
        val commit = createCommit(id = commitId)
        val commitResult = Future.successful(Right(Some(commit)))
        val url = singleCommitRoute(commitId = commitId)
        val response = new CommitResult(List(), commit)
        when(search.commit(host, project, repository, commitId)).thenReturn(commitResult)
        withFakeApplication(new FakeGlobalWithSearchService(search)) {
          val result = route(FakeRequest(GET, url)).get
          status(result) mustEqual OK
          contentType(result) mustEqual Some("application/x.zalando.commit+json")
          contentAsString(result) mustEqual Json.stringify(Json.toJson(response))
        }
      }

      "Return 404 when the object is not found" in {
        val search = mock[Search]
        val commitId = "commitId"
        val commit = createCommit(id = commitId)
        val commitResult = Future.successful(Right(None))
        val url = singleCommitRoute(commitId = commitId)
        val response = new CommitResult(List(), commit)
        when(search.commit(host, project, repository, commitId)).thenReturn(commitResult)
        withFakeApplication(new FakeGlobalWithSearchService(search)) {
          val Some(result) = route(FakeRequest(GET, url))
          status(result) mustEqual NOT_FOUND
          contentAsString(result) mustBe empty
        }
      }

      "Return 500 when an unexpected error occures" in {
        val search = mock[Search]
        val commitId = "commitId"
        val commit = createCommit(id = commitId)
        val commitResult = Future.successful(Left("error"))
        val url = singleCommitRoute(commitId = commitId)
        val response = new Error("An error occurred, please check the logs", 500, "undefined")
        when(search.commit(host, project, repository, commitId)).thenReturn(commitResult)
        withFakeApplication(new FakeGlobalWithSearchService(search)) {
          val result = route(FakeRequest(GET, url)).get
          status(result) mustEqual INTERNAL_SERVER_ERROR
          contentType(result) mustEqual Some("application/problem+json")
          contentAsString(result) mustEqual Json.stringify(Json.toJson(response))
        }
      }

    }
}