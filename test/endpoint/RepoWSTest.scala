package endpoint

import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.test._
import play.api.test.Helpers._
import test.util.MockitoUtils._
import java.net.URLEncoder
import org.specs2.matcher.MustExpectable
import org.scalatest.Matchers
class ReponWSTest extends PlaySpec  with OneAppPerSuite {
  val reposRoute = "/api/repos/"
  withFakeApplication {
    "HEAD /api/repos" should {
      "Return 400 (Bad Request) on invalid(not parsable) URI" in {
        val Some(result) = route(FakeRequest(HEAD, reposRoute + "asdfj-com"))
        status(result) mustEqual BAD_REQUEST
      }
      "Return 200 (OK) on a already normalized URI" in {
        val uri = URLEncoder.encode("https://github.com/zalando/kontrolletti", "UTF-8");
        val Some(result) = route(FakeRequest(HEAD, s"$reposRoute$uri"))
        status(result) mustEqual OK
      }
      "Return 301 (Moved permanently) on a URI that is not normalized" in {
        val uri = URLEncoder.encode("git@github.com:zalando/kontrolletti.git", "UTF-8");
        val Some(result) = route(FakeRequest(HEAD, s"$reposRoute$uri"))
        status(result) mustEqual MOVED_PERMANENTLY
       headers(result) must contain ("Location" -> "https://github.com/zalando/kontrolletti")
      }
    }

  }
}