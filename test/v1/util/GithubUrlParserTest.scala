

package v1.util

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import Accumulation._


class GithubUrlParserTest extends FunSuite {

  sealed case class GithubProject(name: String, url: String)

  private val ghHost = "git-hub.com"
  private val ghProject = "zalando-bus"
  private val ghRepo = "kontrolletti"
  private val hosts = List("git-hub.com", "git-hub.com:8080", "git-hub.com:22")
  private val project = List("zalando-bus", "stups", "..test", "---", "___", "--")
  private val ghUrls = List(
    GithubProject("test0", "") //
    , GithubProject("test1", "https://git-hub.com/zalando-bus/kontrolletti/") //
    , GithubProject("test2", "https:///zalando-bus/kontrolletti") //
    , GithubProject("test3", "https://git-hub.com:8080/zalando-bus/kontrolletti/") //
    , GithubProject("test4", "git@git-hub.com:zalando-bus/kontrolletti.git") //
    , GithubProject("test5", "git@git-hub.com:22/zalando-bus/kontrolletti.git") //
    , GithubProject("test6", "ssh://git@git-hub.com:22/zalando-bus/kontrolletti.git") //
    , GithubProject("test7", "git-hub.com/zalando-bus/kontrolletti") //
    , GithubProject("test8", "git-hub.com/zalando-bus/kontrolletti/") //
    )

  test("test-0") {
    test("https://git-hub.com/zalando-bus/kontrolletti", "git-hub.com", "zalando-bus", "kontrolletti")
  }
  test("test-1") {
    test("https://git-hub.com/zalando-bus/kontrolletti/", "git-hub.com", "zalando-bus", "kontrolletti")
  }
  test("test-2") {
    test("https://git-hub.com/zalando-bus/kontrolletti/", "git-hub.com", "zalando-bus", "kontrolletti")
  }
  test("test-3") {
    test("https://git-hub.com:8080/zalando-bus/kontrolletti/", "git-hub.com:8080", "zalando-bus", "kontrolletti")
  }
  test("test-4") {
    test("git@git-hub.com/zalando-bus/kontrolletti.git", "git-hub.com", "zalando-bus", "kontrolletti")
  }
  test("test-5") {
    test("git@git-hub.com:22/zalando-bus/kontrolletti.git", "git-hub.com:22", "zalando-bus", "kontrolletti")
  }
  test("test-6") {
    test("ssh://git@git-hub.com:22/zalando-bus/kontrolletti.git", "git-hub.com:22", "zalando-bus", "kontrolletti")
  }
  test("test-7") {
    test("git-hub.com/zalando-bus/kontrolletti", "git-hub.com", "zalando-bus", "kontrolletti")
  }
  test("test-8") {
    test("git-hub.com/zalando-bus/kontrolletti/", "git-hub.com", "zalando-bus", "kontrolletti")
  }
  test("test-9") {
    test("https://github.com/zalando-bus/kontrolletti", "github.com", "zalando-bus", "kontrolletti")
  }

  def test(url: String, host: String, project: String, repo: String) = {
    val parser = new UrlParser() {}
    val result = parser.parse(url)
    assert(result.isGood, "Parsing failed")
    withGood(result) {
      assert(testHost == host)
      assert(testGroup == project)
      assert(testRepo == repo)
    }
  }

}