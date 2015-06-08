package endpoint

import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.test._
import play.api.test.Helpers._
import test.util.MockitoUtils._
import java.net.URLEncoder
import org.specs2.matcher.MustExpectable
import org.scalatest.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import client.SCM
import service.SearchImpl
class ReponWSTest extends PlaySpec with OneAppPerSuite with MockitoSugar {
  val NORMALIZED_REQUEST_PARAMETER = "Normalized-Repository-Identifier"
  val reposRoute = "/api/repos/"
  val defaultUrl = "https://github.com/zalando/kontrolletti"

  "HEAD /api/repos" should {
    withFakeApplication {
      "Return 400 (Bad Request) on invalid(not parsable) URI" in {

        val Some(result) = route(FakeRequest(HEAD, reposRoute + "asdfj-com"))
        status(result) mustEqual BAD_REQUEST
        header(LOCATION, result) mustBe empty
        contentAsString(result) mustBe empty
      }

      "Return 200 (OK) on a already normalized URI" in {
        val uri = URLEncoder.encode(defaultUrl, "UTF-8");
        val Some(result) = route(FakeRequest(HEAD, s"$reposRoute$uri"))
        status(result) mustEqual OK
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

    val client = mock[SCM]
    object FakeTranslatorGlobal extends play.api.GlobalSettings {
      override def getControllerInstance[A](clazz: Class[A]) = {
        new SearchImpl(client).asInstanceOf[A]
      }
    }
    withFakeApplication(FakeTranslatorGlobal) {
      "Return 404 (Not Found) on a URI that is normalized but not found" in {
        val uri = URLEncoder.encode(defaultUrl, "UTF-8");
        val Some(result) = route(FakeRequest(HEAD, s"$reposRoute$uri"))
        verify(client, times(0))
        status(result) mustEqual NOT_FOUND
        header(LOCATION, result).get === (LOCATION -> reposRoute + URLEncoder.encode(defaultUrl, "UTF-8"))
        header(NORMALIZED_REQUEST_PARAMETER, result).get === (NORMALIZED_REQUEST_PARAMETER -> defaultUrl, "UTF-8")
        contentAsString(result) mustBe empty

      }
    }
  }
}