

package util

import org.junit.runner.RunWith
import org.scalatest.FunSuite 
import utility.UrlParser


class GithubUrlParserTest extends FunSuite {

  sealed case class GithubProject(name: String, url: String)

  private val ghHost = "git-hub.com"
  private val ghProject = "zalando-bus"
  private val ghRepo = "kontrolletti"
  private val hosts = List("git-hub.com", "git-hub.com:8080", "git-hub.com:22")
  private val project = List("zalando-bus", "stups", "..test", "---", "___", "--")
  

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
  test("test-10") {
	  test("git@github.com:zalando/kontrolletti.git", "github.com", "zalando", "kontrolletti")
  }

  def test(url: String, host: String, project: String, repo: String) = {
    val parser = new UrlParser() {}
    val result = parser.extract(url)
    assert(result.isRight, "Parsing failed")
    val (testHost, testGroup, testRepo) = result.right.toOption.get 
    assert(testHost == host)
    assert(testGroup == project)
    assert(testRepo == repo)
  }

}