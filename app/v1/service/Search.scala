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
import v1.model.Author
import v1.util.GithubUrlParser
import scala.concurrent.Future
import scala.util.{ Success, Failure }
trait Search {
  def committers(url: String): Future[List[Author]]

}
 
@Singleton
class SearchImpl @Inject() (githubClient: SCM) extends Search with GithubUrlParser with JsonParserGithub {

  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  import scala.concurrent._

  def committers(url: String): Future[List[Author]] = { 
    Logger.info(s"Searching for $url");
    parse(url) match {
      case ("", "", "") =>
        Future(Nil)
      case (host, group, repo) =>
        val res = githubClient.committers(group, repo)
        res onComplete {
          case Success(posts) =>
            Logger.debug("received" + posts.json.validate[List[Author]].get)
          case Failure(t) =>
            Logger.error("An error as occured: " + t.getMessage())
        }
        res.map(posts => {
          posts.json.validate[List[Author]].get
        })
    }
  }
}