package client

import org.scalatest.FunSuite
import org.scalatest.Matchers

import play.api.libs.json._
import play.api.libs.json.Json._
import test.util.FakeResponseData

/**
 * This class tests the Parsing process implemented in SCMParser file.
 */
class SCMParserTest extends FunSuite with Matchers {
  test("Deserialize multiple json Commits with the GithubParser") {
    val jsonData = Json.parse(FakeResponseData.multiGithubCommit)
    val result = GithubToJsonParser.commitToModel(jsonData)
    assert(result.isRight, "Parser failed!!")

    val commit_0 = result.right.get(0)
    val commit_1 = result.right.get(1)

    assert(commit_0 != null)
    assert(commit_0.id == "50cea1156ca558eb6c67e78ca7e5dabc570ea99a")
    assert(commit_0.message == "Merge pull request #8 from zalando-bus/feature-swagger-first\n\nApi Specification in Swagger")
    //    assert(commit_0.valid == None, "Validation is done internaly")
    assert(commit_0.author != null, "Author should not be empty")
    assert(commit_0.author.email == "kanuku@users.noreply.github.com")
    assert(commit_0.author.name == "Fernando Benjamin")
    assert(commit_0.parentIds.size == 2, "Expected two parent-id's")
    assert(commit_0.parentIds(0) == "88c31c976507b32574bb9c76311da1cfc4832d1d")
    assert(commit_0.parentIds(1) == "2ead1df4182c33bbca16768e4200a09ce3b6e68d")
    assert(commit_0.links == None)

    assert(commit_1 != null)
    assert(commit_1.id == "2ead1df4182c33bbca16768e4200a09ce3b6e68d")
    assert(commit_1.message == "Swagger specification is ready.")
    //    assert(commit_1.valid == None, "Validation is done internaly")
    assert(commit_1.author != null, "Author should not be empty")
    assert(commit_1.author.email == "benibadboy@hotmail.com")
    assert(commit_1.author.name == "Fernando Benjamin")
    assert(commit_1.parentIds.size == 1, "Expected single parent-id")
    assert(commit_1.parentIds(0) == "ca0003e2beba64c96150f03a3cd1d84c58c6a469")
    assert(commit_1.links == None)
  }
  test("Deserialize multiple json Commits with the StashParser") {
    val jsonData = Json.parse(FakeResponseData.multiStashCommit)
    val result = StashToJsonParser.commitToModel(jsonData)
    assert(result.isRight, "Parser failed!!")
    val commit_0 = result.right.get(0)
    val commit_1 = result.right.get(1)

    assert(commit_0 != null)
    assert(commit_0.id == "d7d99a9ee6aa9c3d0960f1591fddf78f65171dd9")
    assert(commit_0.message == "Remove comments")
    //    assert(commit_0.valid == None, "Validation is done internaly")
    assert(commit_0.author != null, "Author should not be empty")
    assert(commit_0.author.email == "benibadboy@hotmail.com")
    assert(commit_0.author.name == "Fernando Benjamin")
    assert(commit_0.parentIds.size == 2, "Expected two parent-id's")
    assert(commit_0.parentIds(0) == "9405c626889dbe91694c7dab33eb091a9483317e")
    assert(commit_0.parentIds(1) == "ab33eb091a9483317e9405c626889dbe91694c7d")
    assert(commit_0.links == None)

    assert(commit_1 != null)
    assert(commit_1.id == "9405c626889dbe91694c7dab33eb091a9483317e")
    assert(commit_1.message == "Testing")
    //        assert(commit_1.valid == None, "Validation is done internaly")
    assert(commit_1.author != null, "Author should not be empty")
    assert(commit_1.author.email == "benibadboy@hotmail.com")
    assert(commit_1.author.name == "Fernando Benjamin")
    assert(commit_1.parentIds.size == 1, "Expected single parent-id")
    assert(commit_1.parentIds(0) == "1a4ed65260f854d35c1ab01a6113964f8fc24414")
    assert(commit_1.links == None)
  }
  test("Deserialize single jsonObject(Repo) with the GithubParser") {
    val jsonData = Json.parse(FakeResponseData.ghRepo)
    val result = GithubToJsonParser.repoToModel(jsonData)
    assert(result.isRight, "Failed to parse!!")
    val repo = result.right.get
    assert(repo != null)
    assert(repo.html_url == "https://github.com/zalando/kontrolletti")
    assert(repo.project == null)
    assert(repo.host == null)
    assert(repo.repository == null)
    assert(repo.commits == None)
    assert(repo.links == None)
  }
  test("Deserialize single jsonObject(Repo) with the StashParser") {
    val jsonData = Json.parse(FakeResponseData.stashRepo)
    val result = StashToJsonParser.repoToModel(jsonData)
    assert(result.isRight, "Failed to parse!!")
    val repo = result.right.get
    assert(repo != null)
    assert(repo.html_url == "https://stash.zalando.net/projects/DOC/repos/ci-cd/browse")
    assert(repo.project == null)
    assert(repo.host == null)
    assert(repo.repository == null)
    assert(repo.commits == None)
    assert(repo.links == None)
  }

}