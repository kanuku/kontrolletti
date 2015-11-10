package client.scm

import org.scalatest.Matchers.convertToAnyShouldWrapper
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{ OneAppPerSuite, PlaySpec }

import configuration.SCMConfigurationImpl
import javax.inject.{ Inject, Singleton }
import test.util.{ ConfigurableFakeApp, MockitoUtils }
import test.util.ConfigurationDefaults.SCMConfigurationDefaults._

class SCMResolverTest extends PlaySpec //
    with OneAppPerSuite with ConfigurableFakeApp with MockitoSugar with MockitoUtils {
  implicit override lazy val app = fakeApplication

  val config = new SCMConfigurationImpl
  val github: SCMResolver = new GithubResolver(config)
  val stash: SCMResolver = new StashResolver(config)

  //Project
  val project = "zalando"
  val repo = "kontrolletti"

  //Commit-ids
  val id = "7a82007"
  val source = "176910a"
  val target = "f85e3aa"
  val since = "06-12-2015"
  val until = "12-12-2015"

  "Resolver#hostType" should {
    "return github" in {
      github.hostType shouldBe "github"
    }
    "return stash" in {
      stash.hostType shouldBe "stash"
    }
  }

  "Resolver#hosts" should {
    "contain github.com and github-Enterprise hosts" in {
      github.hosts.keys should contain(ghost)
      github.hosts.keys should contain(ghehost)
      github.hosts.keys should not contain (shost)
    }
    "contain stash hosts" in {
      stash.hosts.keys should contain(shost)
      stash.hosts.keys should not contain (ghost)
      stash.hosts.keys should not contain (ghehost)
    }
  }
  "Resolver#isCompatible" should {
    "contain github.com and github-Enterprise hosts" in {
      github.isCompatible(ghost) shouldBe true
      github.isCompatible(ghehost) shouldBe true
      github.isCompatible(shost) shouldBe false
    }
    "contain stash hosts" in {
      stash.isCompatible(ghost) shouldBe false
      stash.isCompatible(ghehost) shouldBe false
      stash.isCompatible(shost) shouldBe true
    }
  }

  "Resolver#commits" should {
    "return Github's commits endpoint" in {
      github.commits(ghost, project, repo) shouldBe s"https://api.$ghost/repos/$project/$repo/commits"
    }
    "return Github-Enterprise's commits endpoint" in {
      github.commits(ghehost, project, repo) shouldBe s"https://$ghehost/repos/$project/$repo/commits"
    }
    "refere to Stash's commits endpoint" in {
      stash.commits(shost, project, repo) shouldBe s"https://$shost/rest/api/1.0/projects/$project/repos/$repo/commits"
    }
  }

  "Resolver#commit" should {
    "refere to Github's commit endpoint" in {
      github.commit(ghost, project, repo, id) shouldBe s"https://api.$ghost/repos/$project/$repo/commits/$id"
    }
    "refere to Github-Enterprise's commit endpoint" in {
      github.commit(ghehost, project, repo, id) shouldBe s"https://$ghehost/repos/$project/$repo/commits/$id"
    }
    "refere to Stash's commit endpoint" in {
      stash.commit(shost, project, repo, id) shouldBe s"https://$shost/rest/api/1.0/projects/$project/repos/$repo/commits/$id"
    }
  }

  "Resolver#repo" should {
    "refere to Github's repo endpoint" in {
      github.repo(ghost, project, repo) shouldBe s"https://api.$ghost/repos/$project/$repo"
    }
    "refere to Github-Enterprise's repo endpoint" in {
      github.repo(ghehost, project, repo) shouldBe s"https://$ghehost/repos/$project/$repo"
    }
    "refere to Stash's repo endpoint" in {
      stash.repo(shost, project, repo) shouldBe s"https://$shost/rest/api/1.0/projects/$project/repos/$repo"
    }
  }

  "Resolver#resolve" should {
    "return Github's SCMResolver " in {
      github.resolve(ghost) shouldBe Some(github)
    }
    "return Github-Enterprise's SCMResolver " in {
      github.resolve(ghehost) shouldBe Some(github)
    }
    "return Stash's SCMResolver " in {
      stash.resolve(shost) shouldBe Some(stash)
    }
  }

  "Resolver#repoUrl" should {
    "return Github's repo-URL" in {
      github.repoUrl(ghost, project, repo) shouldBe s"https://$ghost/$project/$repo"
      github.repoUrl("nothing.com", project, repo) shouldBe ""

    }
    "return Github-Enterprise's repo-URL" in {
      github.repoUrl(ghehost, project, repo) shouldBe s"https://$ghehost/$project/$repo"
    }
    "return Stash's repo-URL" in {
      stash.repoUrl(shost, project, repo) shouldBe s"https://$shost/projects/$project/repos/$repo/browse"
      stash.repoUrl("nothing.com", project, repo) shouldBe ""
    }
  }

  "Resolver#diffUrl" should {
    "return Github's diff-URL" in {
      github.diffUrl(ghost, project, repo, source, target) shouldBe s"https://api.$ghost/$project/$repo/compare/$source...$target"
    }
    "return Github-Enterprise's diff-URL" in {
      github.diffUrl(ghehost, project, repo, source, target) shouldBe s"https://$ghehost/$project/$repo/compare/$source...$target"
    }
    "return Stash's diff-URL" in {
      stash.diffUrl(shost, project, repo, source, target) shouldBe s"https://$shost/rest/api/1.0/projects/$project/repos/$repo/compare/commits?from=$source&to=$target"
    }
  }

  "Resolver#accessTokenHeader" should {
    "return Github's accessTokenHeader" in {
      github.accessTokenHeader(ghost) shouldBe ("access_token" -> githubAccessToken)
    }
    "return Github-Enterprise's accessTokenHeader" in {
      github.accessTokenHeader(ghehost) shouldBe ("access_token" -> githubEnterpriseAccessToken)
    }
    "return Stash's accessTokenHeader" in {
      stash.accessTokenHeader(shost) shouldBe ("X-Auth-Token" -> stashAccessToken)
    }
  }

  "Resolver#maximumPerPageQueryParameter" should {
    "return Github's maximumPerPageQueryParameter" in {
      github.maximumPerPageQueryParameter shouldBe ("per_page" -> "100")
    }
    "return Stash's maximumPerPageQueryParameter" in {
      stash.maximumPerPageQueryParameter shouldBe ("limit" -> "10000")
    }
  }

  "Resolver#sinceCommitQueryParameter" should {
    "return Github's sinceCommitQueryParameter" in {
      github.sinceCommitQueryParameter(since) shouldBe ("date" -> since)
    }
    "return Stash's sinceCommitQueryParameter" in {
      stash.sinceCommitQueryParameter(since) shouldBe ("since" -> since)
    }
  }

  "Resolver#startAtPageNumber" should {
    "return Github's startAtPageNumber" in {
      github.startAtPageNumber(26) shouldBe ("page" -> "26")
    }
    "return Stash's startAtPageNumber minus-1" in {
      stash.startAtPageNumber(40) shouldBe ("start" -> "39")
    }
  }

  "Resolver#isGithubServerType" should {
    "return Github's isGithubServerType" in {
      github.isGithubServerType() shouldBe true
    }
    "return Stash's isGithubServerType" in {
      stash.isGithubServerType() shouldBe false
    }
  }

  "Resolver#accessTokenValue" should {
    "return Github's accessTokenValue" in {
      github.accessTokenValue(ghost) shouldBe githubAccessToken
    }
    "return Github-Enterprise's accessTokenValue" in {
      github.accessTokenValue(ghehost) shouldBe githubEnterpriseAccessToken
    }
    "return Stash's accessTokenValue" in {
      stash.accessTokenValue(shost) shouldBe stashAccessToken
    }
  }
  "Resolver#authUserHeaderParameter" should {
    "return Github's authUser" in {
      github.authUserHeaderParameter(ghost) shouldBe ("" -> "")
    }
    "return Github-Enterprise's authUser" in {
      github.authUserHeaderParameter(ghehost) shouldBe ("" -> "")
    }
    "return Stash's authUser " in {
      stash.authUserHeaderParameter(shost) shouldBe ("X-Auth-User" -> stashUser)
    }
  }

  "Resolver#userQueryParameter" should {
    "return Github's userQueryParameter" in {
      github.accessTokenHeader(ghost) shouldBe ("access_token" -> githubAccessToken)
    }
    "return Github-Enterprise's userQueryParameter" in {
      github.accessTokenHeader(ghehost) shouldBe ("access_token" -> githubEnterpriseAccessToken)
    }
    "return Stash's userQueryParameter" in {
      stash.accessTokenHeader(shost) shouldBe ("X-Auth-Token" -> stashAccessToken)
    }
  }
  override def configuration: Map[String, _] = scmConfigurations
}
