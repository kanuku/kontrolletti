

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

  test(getName("test0")) {
    val project=getProject("test0")
    test(project.url, project.name)
  }
  test(getName("test1")) {
    val project=getProject("test1")
    test(project.url, project.name)
  }
  test(getName("test2")) {
    val project=getProject("test2")
    test(project.url, project.name)
  }
  test(getName("test3")) {
    val project=getProject("test3")
    test(project.url, project.name)
  }
  test(getName("test4")) {
    val project=getProject("test4")
    test(project.url, project.name)
  }
  test(getName("test5")) {
    val project=getProject("test5")
    test(project.url, project.name)
  }
  test(getName("test6")) {
    val project=getProject("test6")
    test(project.url, project.name)
  }
  test(getName("test7")) {
	  val project=getProject("test7")
			  test(project.url, project.name)
  }
  test(getName("test8")) {
	  val project=getProject("test8")
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
    val (host, group, repo) = parse(url)
    assert(host == ghHost, s"Testing $name")
    assert(group == ghGroup, s"Testing $name")
    assert(repo == ghRepo, s"Testing $name")
  }

}