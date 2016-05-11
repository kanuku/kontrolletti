package utility

import org.scalatest.FunSuite
import service.SearchImpl

class UrlParserTest extends FunSuite {
  val parser: UrlParser = new UrlParser {}

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
    test("ssh://git@git-hub.com:22/zalando-bus/kontrolletti.git", "git-hub.com", "zalando-bus", "kontrolletti")
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
    test("ssh://git@stash.com/cd/ansible-playbooks.git", "stash.com", "cd", "ansible-playbooks")
  }
  test("test-12") {
    test("https://kanuku@stash.com/scm/cd/ansible-playbooks.git", "stash.com", "cd", "ansible-playbooks")
  }
  test("test-13") {
    test("https://mystash.com/scm/~project/repository.git", "mystash.com", "~project", "repository")
  }
  test("test-14") {
    test("https://git@mystash.com:4999/~nzimnicki/stock-chart-demo.git", "mystash.com:4999", "~nzimnicki", "stock-chart-demo")
  }
  test("test-15") {
    test("ssh://git@mystash.com:4999/~lschumacher/eventlog-remote-writer-demoapp.git", "mystash.com", "~lschumacher", "eventlog-remote-writer-demoapp")
  }
  test("test-16") { //Bug -> https://github.com/zalando/kontrolletti/issues/138
    test("https://stash.com/scm/~lschumacher/20questions.git", "stash.com", "~lschumacher", "20questions")
  }

  test("test-17") { //Bug -> https://github.com/zalando/kontrolletti/issues/138
    test("ssh://lschumacher@stash.com:7999/~lschumacher/eventlog-remote-writer-demoapp.git", "stash.com", "~lschumacher", "eventlog-remote-writer-demoapp")
  }
  test("test-18") {
    test("https://stash.com/scm/le/zalos-moenchengladbach-backend-openig.git/", "stash.com", "le", "zalos-moenchengladbach-backend-openig")
  }

  test("test-19") { // Issue -> https://github.com/zalando/kontrolletti/issues/185
    test("git:git@github.com/zalando/kontrolletti.git", "github.com", "zalando", "kontrolletti")
  }

  def test(url: String, host: String, project: String, repo: String) = {

    val result = parser.extract(url)
    assert(result.isRight, "Parsing failed")
    val (testHost, testGroup, testRepo) = result.right.get
    assert(testHost == host)
    assert(testGroup == project)
    assert(testRepo == repo)
  }

}
