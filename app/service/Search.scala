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
import v1.client.SCMResolver

trait Search {
  def committers(url: String): Future[Either[String, List[Author]]]
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
  def committers(url: String): Future[Either[String, List[Author]]] = {
    Logger.info(s"Searching for $url");
    extract(url) match {
      case Left(error) =>
        Logger.error(error)
        Future { Left(error) }
      case Right((host, group, repo)) =>
        resolveParser(host) match {
          case Left(error) => Future { Left(error) }
          case Right(parser) =>
            lazy val call: Callable = () => client.committers(host, group, repo)
            requestFromUrl(call)(parser.authorToModel)
        }
    }
  }

  def commits(url: String): Future[Either[String, List[Commit]]] = {
    Logger.info(s"Searching for $url");
    
    extract(url) match {
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
  
  def handleRequest(url:String) = ???
  def requestFromUrl[A](call: Callable)(implicit parser: Parser[JsValue, Either[String, A]]): Future[Either[String, A]] = {
    val futureResponse = call()
    futureResponse onComplete {
      case Success(response) =>
        Logger.info("Received HTTP Code " + response.status)
      case Failure(t) => // Clients of this service don't need to know the details
        Logger.error("Error while calling SCM " + t.getMessage)
    }
    futureResponse.map { response =>
      if (response.status != 200)
        Left("Something went wrong, HTTP CODE =>" + response.status)
      else {
        parser(response.json) match {
          case Left(error) =>
            Logger.info(error)
            Left("Could not parse the json result(scm)!")
          case Right(obj) => Right(obj)
        }
      }
    } recover {
      case e =>
        Logger.error(e.getMessage)
        Left("An internal error occurred!")
    }
  }

  def resolveParser(host: String): Either[String, SCMParser] =
    (GithubToJsonParser.resolve(host) orElse StashToJsonParser.resolve(host)) match {
      case Some(parser) => Right(parser)
      case None         => Left("Could not resolve the SCMResolver")
    }

}