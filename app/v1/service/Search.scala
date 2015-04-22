package v1.service

import akka.dispatch.OnComplete
import akka.dispatch.OnFailure
import javax.inject._
import play.api.Logger
import play.api.libs.json._
import play.api.libs.json._
import play.api.libs.json.Reads._
import v1.client.SCM
import v1.model.JsonParserGithub
import v1.model.User
import v1.util.GithubUrlParser
import scala.concurrent.Future
import scala.util.{ Success, Failure }
trait Search {

  /**
   *
   */

  def users(url: String): Future[List[User]]

}

@Singleton
class SearchImpl @Inject() (githubClient: SCM) extends Search with GithubUrlParser with JsonParserGithub {

  //            println(Json.parse(test).validate[List[User]])
  //            println(new User("test", 1345))

  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  import scala.concurrent._

  def users(url: String): Future[List[User]] = {
    Logger.info(s"Searching for $url");
    parse(url) match {
      case ("", "", "") =>
        Future(Nil)
      case (host, group, repo) =>
        val res = githubClient.contributorsByRepo(group, repo)
        res onComplete {
          case Success(posts) =>
            println(posts.json.validate[List[User]].get)
          case Failure(t) =>
            Logger.error("An error as occured: " + t.getMessage())
        }

        res.map(posts => {
          posts.json.validate[List[User]].get
        })
    }
  }
}

class FakeSearchServiceImpl extends Search {
  def users(url: String): Future[List[User]] = ???
}