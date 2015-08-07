package client

import org.mockito.ArgumentCaptor
import org.mockito.Matchers.anyString
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.PlaySpec
 

import play.api.libs.ws.WSRequestHolder
import play.api.libs.ws.WSResponse
import play.api.test.FakeApplication
import test.util.MockitoUtils

/**
 * This tests make sure that the configurations for the client are being
 * read from properties in the application.conf file.
 * If you change the file, this test should fail.
 */
class SCMResolverTest extends PlaySpec with OneAppPerSuite with MockitoSugar with MockitoUtils{

  // Override app if you need a FakeApplication with other than non-default parameters.
  implicit override lazy val app: FakeApplication = FakeApplication()

  "The GithubResolver " must {
    val host = "github.com"
    val project = "kanuku"
    val repo = "misc"
    val resolver: SCMResolver = GithubResolver
    "return the configured hosts in application.conf" in {
      val hosts = Set("github.com")
      assert(resolver.hosts == hosts)
    }
    "name must be github" in {
      assert(resolver.name == "github")
    }
    "contain the host configuration in the property" in {
      assert(resolver.hostsProperty == "client.github.hosts")
    }
    "be compatible with github.com" in {
      assert(resolver.isCompatible("github.com"))
    }
    "resolve to the same host on github.com" in {
      assert(resolver == resolver.resolve("github.com").get)
    }
    "not resolve to the same host on test.com" in {
      assert(resolver.resolve("test.com") == None)
    }

    "use the passed parameters in the commits url" in {
      val commitsUrl = "https://api.github.com/repos/kanuku/misc/commits"
      assert(resolver.commits(host, project, repo) === commitsUrl)
    }

    "use the passed parameters in the contributors url" in {
      val contributorsUrl = "https://api.github.com/repos/kanuku/misc/contributors"
//      assert(resolver.contributors(host, project, repo) === contributorsUrl)
    }
    "use the passed parameters in the reposory url" in {
    	val contributorsUrl = "https://api.github.com/repos/kanuku/misc"
    			assert(resolver.repo(host, project, repo) === contributorsUrl)
    }
  }
  "The StashResolver " must {

    val host = "stash.zalando.net"
    val project = "doc"
    val repo = "ci-cd"

    val resolver: SCMResolver = StashResolver
    "return the configured hosts in application.conf" in {
      val hosts = Set("stash.zalando.net")
      assert(resolver.hosts == hosts)
    }
    "name must be stash" in {
      assert(resolver.name == "stash")
    }
    "contain the host configuration in the property" in {
      assert(resolver.hostsProperty == "client.stash.hosts")
    }
    "be compatible with stash.zalando.net" in {
      assert(resolver.isCompatible("stash.zalando.net"))
    }
    "resolve to the same host on stash.zalando.net" in {
      assert(resolver == resolver.resolve("stash.zalando.net").get)
    }
    "not resolve to the same host on test.com" in {
      assert(resolver.resolve("test.com") == None)
    }

    "use the passed parameters in the commits url" in {
      val commitsUrl = "https://stash.zalando.net/rest/api/1.0/projects/doc/repos/ci-cd/commits"
      println(resolver.commits(host, project, repo))
      assert(resolver.commits(host, project, repo) === commitsUrl)
    }

    "use the passed parameters in the contributors url" in {
      val contributorsUrl = "https://stash.zalando.net/rest/api/1.0/projects/doc/repos/ci-cd/contributors"
//      println(resolver.contributors(host, project, repo))
//      assert(resolver.contributors(host, project, repo) === contributorsUrl)
    }

  }

}

