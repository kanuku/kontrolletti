

package v1.util

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner

import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class GithubUrlParserTest extends FunSuite with GithubUrlParser {

  case class GithubProject(name: String, url: String)
  val ghHost = "git-hub.com"
  val ghGroup = "zalando-bus"
  val ghRepo = "kontrolletti"
  val ghUrls = List(
    GithubProject("test0", "git-hub.com/zalando-bus/kontrolletti"),
    GithubProject("test1", "git-hub.com/zalando-bus/kontrolletti/"),
    GithubProject("test2", "https://git-hub.com/zalando-bus/kontrolletti"), //
    GithubProject("test3", "https://git-hub.com/zalando-bus/kontrolletti/"), //
    GithubProject("test4", "https://git-hub.com:8080/zalando-bus/kontrolletti"),
    GithubProject("test5", "https://git-hub.com:8080/zalando-bus/kontrolletti/"),
    GithubProject("test6", "git@git-hub.com:zalando-bus/kontrolletti.git"),
    GithubProject("test7", "git@git-hub.com:22/zalando-bus/kontrolletti.git"),
    GithubProject("test8", "ssh://git@git-hub.com:22/zalando-bus/kontrolletti.git"))

  test(ghUrls(0).name + " -> " + ghUrls(0).url) {
    test(ghUrls(0).url, ghUrls(0).name)
  }
  test(ghUrls(1).name + " -> " + ghUrls(1).url) {
    test(ghUrls(1).url, ghUrls(1).name)
  }
  test(ghUrls(2).name + " -> " + ghUrls(2).url) {
    test(ghUrls(2).url, ghUrls(2).name)
  }
  test(ghUrls(3).name + " -> " + ghUrls(3).url) {
    test(ghUrls(3).url, ghUrls(3).name)
  }
  test(ghUrls(4).name + " -> " + ghUrls(4).url) {
    test(ghUrls(4).url, ghUrls(4).name)
  }
  test(ghUrls(5).name + " -> " + ghUrls(5).url) {
    test(ghUrls(5).url, ghUrls(5).name)
  }
  test(ghUrls(6).name + " -> " + ghUrls(6).url) {
    test(ghUrls(6).url, ghUrls(6).name)
  }

  def test(url: String, name: String) = {
    val (host, group, repo) = parse(url)
    assert(host == ghHost, s"Testing $name")
    assert(group == ghGroup, s"Testing $name")
    assert(repo == ghRepo, s"Testing $name")
  }

}