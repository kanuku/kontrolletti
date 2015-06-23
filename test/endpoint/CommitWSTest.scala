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

class CommitWSTest extends PlaySpec with OneAppPerSuite with MockitoSugar with MockitoUtils with ScalaFutures {
  val host = "github.com"
  val project = "zalando"
  val repo = "kontrolletti"

  s"GET /api/hosts/*/*/*/commits" should {
    "Return 200 on a successfull request with Result" in {
      val commits = List(new Commit("id", "message", List("parentId"), new Author("name", "email", List()), None, List(new Link("href", "method", "rel", "relType"))))
      val response = Future.successful(Right(commits))
      
      // Mock search.commits to return list of commits
      val search = mock[Search]
      when(search.commits(host, project, repo)).thenReturn(response)

      //Let Guice return mocked searchService
      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        val Some(result) = route(FakeRequest(GET, s"/api/hosts/$host/projects/$project/repos/$repo/commits"))
        status(result) mustEqual OK
        contentType(result) mustEqual Some("application/x.zalando.commit+json")
        
        import model.KontrollettiToModelParser._
        contentAsString(result) mustEqual Json.prettyPrint(Json.toJson(commits))

        //searchService should be called only once
        verify(search, times(1)).commits(host, project, repo)
      }
    }

    "Return 400 on a successfull request with empty Result" in {
      val commits = List()
      val response = Future.successful(Right(commits))
      
      // Mock search.commits to return an empty list
      val search = mock[Search]
      when(search.commits(host, project, repo)).thenReturn(response)

      //Let Guice return mocked searchService
      withFakeApplication(new FakeGlobalWithSearchService(search)) {
        val Some(result) = route(FakeRequest(GET, s"/api/hosts/$host/projects/$project/repos/$repo/commits"))
        status(result) mustEqual NOT_FOUND
        contentType(result) mustEqual Some("application/x.zalando.commit+json")
        contentAsString(result) mustBe empty

        //searchService should be called only once
        verify(search, times(1)).commits(host, project, repo)
      }
    }
  }
  
  

}