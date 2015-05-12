

package v1.util

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner

import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class GithubUrlParserTest extends FunSuite {

  sealed case class GithubProject(name: String, url: String)

  private val ghHost = "git-hub.com"
  private val ghProject = "zalando-bus"
  private val ghRepo = "kontrolletti"

  private val ghUrls = List(
    GithubProject("test0", "https://git-hub.com/zalando-bus/kontrolletti") //
    , GithubProject("test1", "https://git-hub.com/zalando-bus/kontrolletti/") //
    , GithubProject("test2", "https://git-hub.com:8080/zalando-bus/kontrolletti") //
    , GithubProject("test3", "https://git-hub.com:8080/zalando-bus/kontrolletti/") //
    , GithubProject("test4", "git@git-hub.com:zalando-bus/kontrolletti.git") //
    , GithubProject("test5", "git@git-hub.com:22/zalando-bus/kontrolletti.git") //
    , GithubProject("test6", "ssh://git@git-hub.com:22/zalando-bus/kontrolletti.git") //
    , GithubProject("test7", "git-hub.com/zalando-bus/kontrolletti") //
    , GithubProject("test8", "git-hub.com/zalando-bus/kontrolletti/") //
    )

  test(getName("test0")) {
    val project = getProject("test0")
    test(project.url, project.name)
  }
  test(getName("test1")) {
    val project = getProject("test1")
    test(project.url, project.name)
  }
  test(getName("test2")) {
    val project = getProject("test2")
    test(project.url, project.name)
  }
  test(getName("test3")) {
    val project = getProject("test3")
    test(project.url, project.name)
  }
  test(getName("test4")) {
    val project = getProject("test4")
    test(project.url, project.name)
  }
  test(getName("test5")) {
    val project = getProject("test5")
    test(project.url, project.name)
  }
  test(getName("test6")) {
    val project = getProject("test6")
    test(project.url, project.name)
  }
  test(getName("test7")) {
    val project = getProject("test7")
    test(project.url, project.name)
  }
  test(getName("test8")) {
    val project = getProject("test8")
    test(project.url, project.name)
  }

  def getName(name: String): String = {
    val res = getProject(name)
    res.name + " -> " + res.url
  }
  def getProject(name: String): GithubProject = {
    ghUrls.find { x => x.name == name }.get
  }

  def test(url: String, name: String) = {
    val parser = new GithubUrlParser() {}
    val (host, group, repo) = parser.parse(url)
    assert(host == ghHost, s"Testing $name")
    assert(group == ghProject, s"Testing $name")
    assert(repo == ghRepo, s"Testing $name")
  }

}