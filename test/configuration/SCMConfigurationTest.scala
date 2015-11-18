package configuration

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import test.util.ConfigurableFakeApp
import org.scalatest.Matchers._

class SCMConfigurationTest extends PlaySpec with ConfigurableFakeApp with OneAppPerSuite {
  val scmConfig: SCMConfiguration = new SCMConfigurationImpl

  implicit override lazy val app = fakeApplication

  //ServerTypes
  val stashType = "stash"
  val githubType = "github"

  //Hosts
  val githubCom = "github.com"
  val githubEnterprise = "g-e-company.com"
  val githubMyCompany = "my-company.io"
  val stashCom = "stash.com"
  val stashPrivate = "stash-company.com"
  val stashMyCompany = "stash-company.io"

  override def configuration: Map[String, _] = Map(

    /*##############################
     * Hostnames
     */
    // Client hosts Github-type
    "client.scm.github.host.0" -> githubCom, // Server Github
    "client.scm.github.host.1" -> githubEnterprise, // Server Github-Enterprise1
    "client.scm.github.host.2" -> githubMyCompany, // Server Github-Enterprise2

    // Client hosts Stash-type
    "client.scm.stash.host.0" -> stashCom, // Server Stash1
    "client.scm.stash.host.1" -> stashPrivate, // Server Stash2
    "client.scm.stash.host.2" -> stashMyCompany, // Server Stash3

    /*##############################
     * URL Precedent
     */
    //Rest API Succeeder Github-type
    "client.scm.github.urlPrecedent.0" -> "https://api.", // Server Github
    "client.scm.github.urlPrecedent.1" -> "https://", // Server Github-Enterprise1
    "client.scm.github.urlPrecedent.2" -> "http://", // Server Github-Enterprise2

    //Rest API precedent Stash-type
    "client.scm.stash.urlPrecedent.0" -> "https://server.", // Server Stash1
    "client.scm.stash.urlPrecedent.1" -> "ftp://", // Server  Stash2 (Unrealistic precedent)
    "client.scm.stash.urlPrecedent.2" -> "ssh://", // Server Stash3(Unrealistic)

    /*##############################
     * URL Succeeder
     */
    //Rest API precedent Github-type
    "client.scm.github.urlSucceeder.0" -> "", // Server Github
    "client.scm.github.urlSucceeder.1" -> "/api/v3", // Server Github-Enterprise1
    "client.scm.github.urlSucceeder.2" -> "/api/v4", // Server Github-Enterprise2 (Unrealistic)

    //Rest API precedent Stash-type
    "client.scm.stash.urlSucceeder.0" -> "/rest/api/1.0", // Server Stash1
    "client.scm.stash.urlSucceeder.1" -> "/rest/api/1.1", // Server  Stash2
    "client.scm.stash.urlSucceeder.2" -> "/rest/api/1.2", // Server Stash3

    /*##############################
     * Authorization token
     */
    //Auth-tokens Github-type
    "client.scm.github.authToken.0" -> "GithubAccessToken0", // Server Github
    "client.scm.github.authToken.1" -> "GithubAccessToken1", // Server Github-Enterprise1
    "client.scm.github.authToken.2" -> "GithubAccessToken2", // Server Github-Enterprise2

    //Auth-tokens Stash-type
    "client.scm.stash.authToken.0" -> "StashAccessToken0", // Server Stash1
    "client.scm.stash.authToken.1" -> "StashAccessToken1", // Server Stash2
    "client.scm.stash.authToken.2" -> "StashAccessToken2", // Server Stash3

    /*##############################
     * Authorization user
     */
    //Auth-user is for Stash only. Github does not need one.
    "client.scm.stash.user.0" -> "StashUser0", // Server Stash1
    "client.scm.stash.user.1" -> "StashUser1", // Server Stash2
    "client.scm.stash.user.2" -> "StashUser2", /// Server Stash3

    /*##############################
     * Allowed projects
     */
    //Allowed projects on github type
    "client.scm.github.allowedProjects.0" -> Set("pGithub-1", "pGithub-2"),
    "client.scm.github.allowedProjects.1" -> Set(),
    "client.scm.github.allowedProjects.2" -> Set(),

    //Allowed projects on stash (Unrealistic)
    "client.scm.stash.allowedProjects.0" -> Set(),
    "client.scm.stash.allowedProjects.1" -> Set(),
    "client.scm.stash.allowedProjects.2" -> Set("CD", "ZALOS"))

  "SCMConfiguration#hosts" should {
    "return github hosts are configured" in {
      val result = scmConfig.hosts(githubType)
      result.size shouldBe 3
      result(githubCom) shouldBe 0
      result(githubEnterprise) shouldBe 1
      result(githubMyCompany) shouldBe 2
    }
    "return stash hosts are configured" in {
      val result = scmConfig.hosts(stashType)
      result.size shouldBe 3
      result(stashCom) shouldBe 0
      result(stashPrivate) shouldBe 1
      result(stashMyCompany) shouldBe 2

      scmConfig.hosts("noon") shouldBe empty
    }
  }

  "SCMConfiguration#urlPrecedent" should {
    "return the precedents for the github type" in {
      //Github
      val result = scmConfig.urlPrecedent(githubType)
      result.size shouldBe 3
      result(0) shouldBe "https://api."
      result(1) shouldBe "https://"
      result(2) shouldBe "http://"
    }
    "return the precedents for the stash type" in {
      //Stash
      val result = scmConfig.urlPrecedent(stashType)
      result.size shouldBe 3
      result(0) shouldBe "https://server."
      result(1) shouldBe "ftp://"
      result(2) shouldBe "ssh://"
    }
  }
  "SCMConfiguration#urlSucceeder" should {
    "return the succeeders for the github type" in {
      //Github
      val result = scmConfig.urlSucceeder(githubType)
      result.size shouldBe 3
      result(0) shouldBe ""
      result(1) shouldBe "/api/v3"
      result(2) shouldBe "/api/v4"
    }
    "return the succeeders for the stash type" in {
      //Stash
      val result = scmConfig.urlSucceeder(stashType)
      result.size shouldBe 3
      result(0) shouldBe "/rest/api/1.0"
      result(1) shouldBe "/rest/api/1.1"
      result(2) shouldBe "/rest/api/1.2"
    }
  }

  "SCMConfiguration#authToken" should {
    "Return auth-token for corresponding scm server type (github)" in {
      val result = scmConfig.authToken(githubType)
      result.size shouldBe 3
      result(0) shouldBe "GithubAccessToken0"
      result(1) shouldBe "GithubAccessToken1"
      result(2) shouldBe "GithubAccessToken2"
    }
    "Return auth-token for corresponding scm server type (stash)" in {
      val result = scmConfig.authToken(stashType)
      result.size shouldBe 3
      result(0) shouldBe "StashAccessToken0"
      result(1) shouldBe "StashAccessToken1"
      result(2) shouldBe "StashAccessToken2"
    }
  }

  "SCMConfiguration#authUser" should {
    "Return user for the corresponding scm server" in {
      val result = scmConfig.authUser(stashType)
      result.size shouldBe 3
      result(0) shouldBe "StashUser0"
      result(1) shouldBe "StashUser1"
      result(2) shouldBe "StashUser2"
    }
  }
  "SCMConfiguration#allowedProjects" should {
    "Return set of projects for github hosts" in {
      val result = scmConfig.allowedProjects(githubType)
      result.size shouldBe 3

      result(0).size shouldBe 2
      result(0) should contain("pGithub-1")
      result(0) should contain("pGithub-2")

      result(1).size shouldBe 0
      result(2).size shouldBe 0
    }
    "Return set of projects for stash hosts" in {
      val result = scmConfig.allowedProjects(stashType)
      result.size shouldBe 3

      result(0).size shouldBe 0

      result(1).size shouldBe 0
      result(2).size shouldBe 2
      result(2) should contain("CD")
      result(2) should contain("ZALOS")
    }
  }
}
