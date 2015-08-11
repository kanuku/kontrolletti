package client

import org.scalatest.FunSuite
import org.scalatest.Matchers
import play.api.libs.json._
import play.api.libs.json.Json._
import test.util.FakeResponseData
import model.Commit

/**
 * This class tests the Parsing process implemented in SCMParser file.
 */
class SCMParserTest extends FunSuite with Matchers {
  test("Deserialize multiple json Commits with the GithubParser") {
    val jsonData = Json.parse(FakeResponseData.multiGithubCommit)
    val result = GithubToJsonParser.commitToModel(jsonData)
    assert(result.isRight, "Parser failed!!")

    val commit0 = result.right.get(0)
    val commit1 = result.right.get(1)

    assert(Option(commit0) != None)
    assert(commit0.id == "50cea1156ca558eb6c67e78ca7e5dabc570ea99a")
    assert(commit0.message == "Merge pull request #8 from zalando-bus/feature-swagger-first\n\nApi Specification in Swagger")
    //    assert(commit0.valid == None, "Validation is done internaly")
    assert(Option(commit0.author) != None, "Author should not be empty")
    assert(commit0.author.email == "kanuku@users.noreply.github.com")
    assert(commit0.author.name == "Fernando Benjamin")
    assert(commit0.parentIds.size == 2, "Expected two parent-id's")
    assert(commit0.parentIds(0) == "88c31c976507b32574bb9c76311da1cfc4832d1d")
    assert(commit0.parentIds(1) == "2ead1df4182c33bbca16768e4200a09ce3b6e68d")
    assert(commit0.links == None)

    assert(Option(commit1) != None)
    assert(commit1.id == "2ead1df4182c33bbca16768e4200a09ce3b6e68d")
    assert(commit1.message == "Swagger specification is ready.")
    //    assert(commit1.valid == None, "Validation is done internaly")
    assert(Option(commit1.author) != None, "Author should not be empty")
    assert(commit1.author.email == "benibadboy@hotmail.com")
    assert(commit1.author.name == "Fernando Benjamin")
    assert(commit1.parentIds.size == 1, "Expected single parent-id")
    assert(commit1.parentIds(0) == "ca0003e2beba64c96150f03a3cd1d84c58c6a469")
    assert(commit1.links == None)
  }
  test("Deserialize multiple json Commits with the StashParser") {
    val jsonData = Json.parse(FakeResponseData.multiStashCommit)
    val result = StashToJsonParser.commitToModel(jsonData)
    assert(result.isRight, "Parser failed!!")
    val commit0 = result.right.get(0)
    val commit1 = result.right.get(1)

    assert(Option(commit0) != None)
    assert(commit0.id == "d7d99a9ee6aa9c3d0960f1591fddf78f65171dd9")
    assert(commit0.message == "Remove comments")
    //    assert(commit0.valid == None, "Validation is done internaly")
    assert(Option(commit0.author) != None, "Author should not be empty")
    assert(commit0.author.email == "benibadboy@hotmail.com")
    assert(commit0.author.name == "Fernando Benjamin")
    assert(commit0.parentIds.size == 2, "Expected two parent-id's")
    assert(commit0.parentIds(0) == "9405c626889dbe91694c7dab33eb091a9483317e")
    assert(commit0.parentIds(1) == "ab33eb091a9483317e9405c626889dbe91694c7d")
    assert(commit0.links == None)

    assert(Option(commit1) != None)
    assert(commit1.id == "9405c626889dbe91694c7dab33eb091a9483317e")
    assert(commit1.message == "Testing")
    //        assert(commit1.valid == None, "Validation is done internaly")
    assert(Option(commit1.author) != None, "Author should not be empty")
    assert(commit1.author.email == "benibadboy@hotmail.com")
    assert(commit1.author.name == "Fernando Benjamin")
    assert(commit1.parentIds.size == 1, "Expected single parent-id")
    assert(commit1.parentIds(0) == "1a4ed65260f854d35c1ab01a6113964f8fc24414")
    assert(commit1.links == None)
  }
  test("Deserialize single jsonObject(Repo) with the GithubParser") {
    val jsonData = Json.parse(FakeResponseData.ghRepo)
    val result = GithubToJsonParser.repoToModel(jsonData)
    assert(result.isRight, "Failed to parse!!")
    val repo = result.right.get
    assert(Option(repo) != None)
    assert(repo.html_url == "https://github.com/zalando/kontrolletti")
    assert(repo.project == "")
    assert(repo.host == "")
    assert(repo.repository == "")
    assert(repo.commits == None)
    assert(repo.links == None)
  }
  test("Deserialize single jsonObject(Repo) with the StashParser") {
    val jsonData = Json.parse(FakeResponseData.stashRepo)
    val result = StashToJsonParser.repoToModel(jsonData)
    assert(result.isRight, "Failed to parse!!")
    val repo = result.right.get
    assert(Option(repo) != None)
    assert(repo.html_url == "https://stash.zalando.net/projects/DOC/repos/ci-cd/browse")
    assert(repo.project == "")
    assert(repo.host == "")
    assert(repo.repository == "")
    assert(repo.commits == None)
    assert(repo.links == None)
  }

  test("Deserialize a single commit with the Githubparser") {
   
    val jsonData = Json.parse(FakeResponseData.singleCommit)

    val result =GithubToJsonParser.singleCommitToModel(jsonData)
    assert(result.isRight, "Failed to parse!!")
    val commit = result.right.get 
    assert(Option(commit) != None)
    assert(commit.id == "50cea1156ca558eb6c67e78ca7e5dabc570ea99a")
    assert(commit.message == "Merge pull request #8 from zalando-bus/feature-swagger-first\n\nApi Specification in Swagger")
    assert(commit.parentIds.size == 2, "Expected two parent-id's")
    assert(commit.author.email == "kanuku@users.noreply.github.com")
    assert(commit.author.name == "Fernando Benjamin")
    assert(commit.links == None)

  }
  
}