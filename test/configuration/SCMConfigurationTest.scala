package configuration

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import test.util.ConfigurableFakeApp
import org.scalatest.Matchers._

class SCMConfigurationTest extends PlaySpec with ConfigurableFakeApp with OneAppPerSuite {
  val scmConfig: SCMConfiguration = new SCMConfigurationImpl

  implicit override lazy val app = fakeApplication

  override def configuration: Map[String, _] = Map(
    // Client hosts Github-type
    "client.scm.github.host.0" -> "github.com", // Server Github
    "client.scm.github.host.1" -> "g-e-company.com", // Server Github-Enterprise1
    "client.scm.github.host.2" -> "my-company.io", // Server Github-Enterprise2

    // Client hosts Stash-type
    "client.scm.stash.host.0" -> "stash.com", // Server Stash1
    "client.scm.stash.host.1" -> "stash-ama.com", // Server Stash2
    "client.scm.stash.host.2" -> "stash-company.io", // Server Stash3

    //Rest API precedent Github-type
    "client.scm.github.urlPrecedent.0" -> "https://api.", // Server Github
    "client.scm.github.urlPrecedent.1" -> "https://", // Server Github-Enterprise1
    "client.scm.github.urlPrecedent.2" -> "http://", // Server Github-Enterprise2

    //Rest API precedent Stash-type
    "client.scm.stash.urlPrecedent.0" -> "https://server.", // Server Stash1
    "client.scm.stash.urlPrecedent.1" -> "ftp://", // Server  Stash2 (Unrealistic precedent)
    "client.scm.stash.urlPrecedent.2" -> "ssh://", // Server Stash3(Unrealistic)

    //Auth-tokens Github-type
    "client.scm.github.authToken.0" -> "GithubAccessToken0", // Server Github
    "client.scm.github.authToken.1" -> "GithubAccessToken1", // Server Github-Enterprise1
    "client.scm.github.authToken.2" -> "GithubAccessToken2", // Server Github-Enterprise2

    //Auth-tokens Stash-type
    "client.scm.stash.authToken.0" -> "StashAccessToken0", // Server Stash1
    "client.scm.stash.authToken.1" -> "StashAccessToken1", // Server Stash2
    "client.scm.stash.authToken.2" -> "StashAccessToken2", // Server Stash3

    //Auth-user is for Stash only. Github does not need one.
    "client.scm.stash.user.0" -> "StashUser0", // Server Stash1
    "client.scm.stash.user.1" -> "StashUser1", // Server Stash2
    "client.scm.stash.user.2" -> "StashUser2" /// Server Stash3
    )

  "OAuthConfiguration#hosts" should {
    "return hosts configured" in {
      scmConfig.hosts("github") should have size 3
      scmConfig.hosts("github") should contain("github.com")
      scmConfig.hosts("github") should contain("g-e-company.com")
      scmConfig.hosts("github") should contain("my-company.io")
      scmConfig.hosts("github") should not contain ("does-not-exist.io")

      scmConfig.hosts("stash") should have size 3
      scmConfig.hosts("stash") should contain("stash.com")
      scmConfig.hosts("stash") should contain("stash-ama.com")
      scmConfig.hosts("stash") should contain("stash-company.io")
      scmConfig.hosts("stash") should not contain ("does-not-exist.io")

      scmConfig.hosts("noon") shouldBe empty
    }
  }

  "OAuthConfiguration#urlPrecedent" should {
    "return the precedents for the corresponding scm server" in {
      //Github
      scmConfig.urlPrecedent("github") should have size 3
      scmConfig.urlPrecedent("github") should contain("https://api.")
      scmConfig.urlPrecedent("github") should contain("https://")
      scmConfig.urlPrecedent("github") should contain("http://")
      scmConfig.urlPrecedent("github") should not contain ("ssh://")
      //Stash
      scmConfig.urlPrecedent("stash") should have size 3
      scmConfig.urlPrecedent("stash") should contain("https://server.")
      scmConfig.urlPrecedent("stash") should contain("ftp://")
      scmConfig.urlPrecedent("stash") should contain("ssh://")
      scmConfig.urlPrecedent("stash") should not contain ("https://")

      scmConfig.urlPrecedent("none") shouldBe empty

    }
  }

  "OAuthConfiguration#authToken" should {
    "Return auth-token for corresponding scm server" in {
      scmConfig.authToken("github") should contain("GithubAccessToken0")
      scmConfig.authToken("github") should contain("GithubAccessToken1")
      scmConfig.authToken("github") should contain("GithubAccessToken2")
      scmConfig.authToken("github") should not contain ("none-existent")

      scmConfig.authToken("stash") should contain("StashAccessToken0")
      scmConfig.authToken("stash") should contain("StashAccessToken1")
      scmConfig.authToken("stash") should contain("StashAccessToken2")
      scmConfig.authToken("stash") should not contain ("none-existent")

      scmConfig.authToken("none") shouldBe empty
    }
  }

  "OAuthConfiguration#authUser" should {
    "Return user for the corresponding scm server" in {
      scmConfig.authUser("stash") should contain("StashUser0")
      scmConfig.authUser("stash") should contain("StashUser1")
      scmConfig.authUser("stash") should contain("StashUser2")

      scmConfig.authUser("none") shouldBe empty
    }

  }
}
