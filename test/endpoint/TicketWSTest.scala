package endpoint

import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.test._
import play.api.test.Helpers._
import java.net.URLEncoder
import org.specs2.matcher.MustExpectable
import org.scalatest.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import client.SCM
import service.SearchImpl
import test.util.MockitoUtils 
import play.api.Logger
import service.Search
import com.google.inject.AbstractModule
import com.google.inject.Guice
import org.mockito.Matchers._
import test.util.MockitoUtils

class TicketWSTest extends PlaySpec with OneAppPerSuite with MockitoSugar with MockitoUtils {
  val NORMALIZED_REQUEST_PARAMETER = "X-Normalized-Repository-Identifier"
  val reposRoute = "/api/repos/"
  val defaultUrl = "https://github.com/zalando/kontrolletti"
  val host = "github.com"
  val project = "zalando"
  val repo = "kontrolletti"
  "HEAD /api/repos" should {
    withFakeApplication {
      "Return 400 (Bad Request) on invalid(not parsable) URI" in {

        val Some(result) = route(FakeRequest(HEAD, reposRoute + "asdfj-com"))
        status(result) mustEqual BAD_REQUEST
        header(LOCATION, result) mustBe empty
        contentAsString(result) mustBe empty
      }
 
      "Return 301 (Moved permanently) on a URI that is not normalized" in {
        val uri = URLEncoder.encode("git@github.com:zalando/kontrolletti.git", "UTF-8");
        val Some(result) = route(FakeRequest(HEAD, s"$reposRoute$uri"))
        status(result) mustEqual MOVED_PERMANENTLY
        header(LOCATION, result).get === (LOCATION -> reposRoute + URLEncoder.encode(defaultUrl, "UTF-8"))
        header(NORMALIZED_REQUEST_PARAMETER, result).get === (NORMALIZED_REQUEST_PARAMETER -> defaultUrl, "UTF-8")
        contentAsString(result) mustBe empty
      }

    }

    "Return 200 on a URI that is normalized and found" in {
      test(200, OK)

    }
    "Return 200 on a URI that is normalized gets redirect by the SCM" in {
      test(301, OK)

    }
    "Return 404 on a URI that is normalized but does not exist" in {
      test(404, NOT_FOUND)
    }
    "Return 500 on a URI when the Service returns 500" in {
      test(500, INTERNAL_SERVER_ERROR)
    }
  }

  def test(returnHttpCode: Int, expectedHttpCode: Int) = {
    val uri = URLEncoder.encode(defaultUrl, "UTF-8");
    val client = mock[SCM]
    val response = mockSuccessfullParsableFutureWSResponse(true, returnHttpCode)
    // record
    when(client.url(host, project, repo)).thenReturn(defaultUrl)
    when(client.repoExists(host, project, repo)).thenReturn(response)

    withFakeApplication(new FakeGlobal(client)) {
      val Some(result) = route(FakeRequest(HEAD, s"$reposRoute$uri"))

      status(result) mustEqual expectedHttpCode
      header(LOCATION, result) mustBe empty
      contentAsString(result) mustBe empty
      verify(client, times(1)).url(anyString, anyString, anyString)
      verify(client, times(1)).repoExists(anyString, anyString, anyString)
    }
  }

  class FakeGlobal(client: SCM) extends play.api.GlobalSettings {
    private lazy val injector = Guice.createInjector(new AbstractModule {
      def configure() {
        bind(classOf[Search]).toInstance(new SearchImpl(client))
      }
    })
    override def getControllerInstance[A](clazz: Class[A]) = {
      injector.getInstance(clazz)
    }

  }

}