package model

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalatestplus.play.OneAppPerSuite
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.JsString
import test.util.MockitoUtils
import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import play.api.libs.json.Json
import KontrollettiToJsonParser._
import KontrollettiToModelParser._
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers
import org.joda.time.DateTime

/**
 * @author fbenjamin
 * If a test fails after parsing to json, during parsing to Model.
 * Check the name and type of the fields defined in both the Reader and Writer of the model.
 * You're welcome.
 *
 */
class KontrollettiToJsonParserTest extends FunSuite with MockitoSugar with MockitoUtils with BeforeAndAfter with Checkers {
  val error = new Error("detail", 500, "http://localhost/errorType")
  val link = new Link("href", "method", "rel", "relType")
  val links = List(link, link)
  val author = new Author("name", "email", Some(List(link, link)))
  val commit = createCommit(id="id",message= "message", parentIds=Option(List("id-1", "id-2")), author= author, links= Some(links))
  val commits = List(commit, commit)
  val repository = createRepository(project="project", host="host", repository="repository",  links=Option(links))
  val repositories = List(repository, repository)
  val ticket = new Ticket("name", "href",Some( List(link, link)))
  val commitsResult = new CommitsResult(links, commits)
  val commitResult = new CommitResult(links, commit)
  val repositoriesResult = new RepositoriesResult(links, repositories)
  val repositoryResult = new RepositoryResult(links, repository)

  test("Error should be parsed") {
    val json = Json.toJson(error)
    assert(json.validate[Error].asOpt == Some(error))
  }
  test("Link should be parsed") {
    val json = Json.toJson(link)
    assert(json.validate[Link].asOpt == Some(link))
  }
  test("Author should be parsed") {
    val json = Json.toJson(author)
    assert(json.validate[Author].asOpt == Some(author))
  }
  test("Commit should be parsed") {
    val json = Json.toJson(commit)
    assert(json.validate[Commit].asOpt == Some(commit))
  }
  test("Repository should be parsed") {
    val json = Json.toJson(repository)
    assert(json.validate[Repository].asOpt == Some(repository))
  }
  test("Ticket should be parsed") {
    val json = Json.toJson(ticket)
    assert(json.validate[Ticket].asOpt == Some(ticket))
  }
  test("CommitsResult should be parsed") {
    val json = Json.toJson(commitsResult)
    assert(json.validate[CommitsResult].asOpt == Some(commitsResult))
  }
  test("CommitResult should be parsed") {
    val json = Json.toJson(commitResult)
    assert(json.validate[CommitResult].asOpt == Some(commitResult))
  }
  test("RepositoriesResult should be parsed") {
    val json = Json.toJson(repositoriesResult)
    assert(json.validate[RepositoriesResult].asOpt == Some(repositoriesResult))
  }
  test("RepositoryResult should be parsed") {
    val json = Json.toJson(repositoryResult)
    assert(json.validate[RepositoryResult].asOpt == Some(repositoryResult))
  }


  implicit val arbDateTime =
    Arbitrary(Gen.choose(Long.MinValue, Long.MaxValue).map(l => new DateTime(l)))

  // property based test
  test("Date writes should produce valid JsString in RFC3339 format") {
    check { d: DateTime =>
      val JsString(str) = Json.toJson(d)
      val offset = str.dropWhile(_ != '+')
      offset.filter(_ == ':').length == 1 // issue #150
    }
  }

}
