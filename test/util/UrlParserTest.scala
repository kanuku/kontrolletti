

package util

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import utility.UrlParser
import client.SCM
import service.SearchImpl

class UrlParserTest extends FunSuite   {
	val parser:UrlParser = new SearchImpl(null)

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

  // stash tests
  test("test-11") {
    test("ssh://git@stash-server.com/cd/ansible-playbooks.git", "stash-server.com", "cd", "ansible-playbooks")
  }
  test("test-12") {
    test("https://kanuku@stash-server.com/scm/cd/ansible-playbooks.git", "stash-server.com", "cd", "ansible-playbooks")
  }

   

  def test(url: String, host: String, project: String, repo: String) = {
    
    val result = parser.extract(url)
    assert(result.isRight, "Parsing failed")
    val (testHost, testGroup, testRepo) = result.right.toOption.get
    assert(testHost == host)
    assert(testGroup == project)
    assert(testRepo == repo)
  }

}