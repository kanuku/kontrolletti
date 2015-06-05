package endpoint

import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.test._
import play.api.test.Helpers._
import test.util.MockitoUtils._
import java.net.URLEncoder
import org.specs2.matcher.MustExpectable
import org.scalatest.Matchers
class ReponWSTest extends PlaySpec with OneAppPerSuite {
  val NORMALIZED_REQUEST_PARAMETER = "Normalized-Repository-Identifier"
  val reposRoute = "/api/repos/"
  val defaultUrl = "https://github.com/zalando/kontrolletti"
  withFakeApplication {
    "HEAD /api/repos" should {
      "Return 400 (Bad Request) on invalid(not parsable) URI" in {
        val Some(result) = route(FakeRequest(HEAD, reposRoute + "asdfj-com"))
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual "Could not parse asdfj-com"
      }
      "Return 200 (OK) on a already normalized URI" in {
        val uri = URLEncoder.encode(defaultUrl, "UTF-8");
        val Some(result) = route(FakeRequest(HEAD, s"$reposRoute$uri"))
        status(result) mustEqual OK
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
  }
}