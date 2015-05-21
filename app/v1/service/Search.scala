package v1.service

import akka.dispatch.OnComplete
import akka.dispatch.OnFailure
import javax.inject._
import play.api.Logger
import play.api.libs.json._
import play.api.libs.json._
import play.api.libs.json.Reads._
import v1.client.SCM
import v1.model.Author
import v1.util.UrlParser
import scala.concurrent.Future
import scala.util.{ Success, Failure }
import play.api.libs.ws.WSResponse
import v1.client.GithubToJsonParser
import v1.client.StashToJsonParser
import v1.model.Commit
import v1.client.SCMParser
import v1.client.SCMParser
 

trait Search {
  def committers(url: String): Either[String, Future[List[Author]]]
  /**
   * Returns commits fetched(cached) from the given url repository.
   * @param url url of the repository
   * @return a future containing the list of commits
   */
  def commits(url: String): Future[Either[String, List[Commit]]]
}

/**
 * This class handles the calls to the right SCM (Stash/Github).
 *
 *
 */
@Singleton
class SearchImpl @Inject() (client: SCM) extends Search with UrlParser {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  import scala.concurrent._
  type Parser[JsValue, B] = JsValue => B
  type Callable = () => Future[WSResponse]
  
  /**
   *
   */
  def committers(repoURL: String): Either[String, Future[List[Author]]] = {
    Logger.info(s"Searching for $repoURL");
    parse(repoURL)
    Left("")

  }

  def commits(url: String): Future[Either[String, List[Commit]]] = {
    Logger.info(s"Searching for $url");
    parse(url) match {
      case Left(error) =>
        Logger.error(error)
        Future { Left(error) }
      case Right((host, group, repo)) =>
        resolveParser(host) match {
          case Left(error) => Future { Left(error) }
          case Right(parser) =>
            lazy val call: Callable = () => client.commits(host, group, repo)
            requestFromUrl(call)(parser.commitToModel)
        }
    }
  }
  
   
  def requestFromUrl[A](call: Callable)(implicit parse: Parser[JsValue, A]): Future[A] = {
    val futureResponse = call()
    futureResponse onComplete {
      case Success(posts) =>
      case Failure(t) => // Clients of this service don't need to know the details
        Logger.error("Error while calling SCM" + t.getMessage)
    }
    futureResponse.map { posts =>
      parse(posts.json)
    }
  }

  def resolveParser =
    (GithubToJsonParser.resolve orElse StashToJsonParser.resolve)
      .andThen { x => if (x.isDefined) Right(x.get) else Left("Could not resolve the SCMParser") }

}