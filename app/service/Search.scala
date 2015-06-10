package service

import akka.dispatch.OnComplete
import akka.dispatch.OnFailure
import javax.inject._
import play.api.Logger
import play.api.libs.json._
import play.api.libs.json._
import play.api.libs.json.Reads._
import client.SCM
import model.Author
import scala.concurrent.Future
import scala.util.{ Success, Failure }
import play.api.libs.ws.WSResponse
import client.GithubToJsonParser
import client.StashToJsonParser
import model.Commit
import client.SCMParser
import client.SCMParser
import client.SCMResolver
import utility.UrlParser

trait Search {
  def committers(url: String): Future[Either[String, List[Author]]]
  /**
   * Returns commits fetched(cached) from the given url repository.
   * @param url url of the repository
   * @return a future containing either the error(left) or list of commits(right)
   */
  def commits(url: String): Future[Either[String, List[Commit]]]
 /**
   * Parse a url into 3 separate parameters, the `host`, `project` and `repo` from a repository-url of a github or stash project
   *
   *  @param url URL of the repository
   *  @return Either a [reason why it couldn't parse] left or a [result (`host`, `project` and `repo`)] right.
   */
  def parse(url:String): Either[String, (String, String, String)]
  
  /**
   * Parses and returns the normalized URI for a github/stash repository-URL.
   * @param url url of the repository
   * @return either an error(left) or the normalized URI (right)
   */
  def normalizeURL(url: String): Either[String, String]

  /**
   * Validates if the repository exists in internal data-store or
   * in the remote origin(SCM).
   * @param url url of the repository
   * @return Either a Left=(Message) if an error occurred or a Right(True/False) if the repository exists.
   */

  def repoExists(url: String): Future[Either[String, Int]]
}

/**
 * This class handles the search logic and retrieves the data from
 * the right source (Stash/Github).
 *
 */
@Singleton
class SearchImpl @Inject() (client: SCM) extends Search with UrlParser {

  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  import scala.concurrent._

  type Parser[JsValue, B] = JsValue => B

  private val logger: Logger = Logger(this.getClass())

  def parse(url: String): Either[String, (String, String, String)] = extract(url)
  
  def committers(url: String): Future[Either[String, List[Author]]] = {
    extract(url) match {
      case Left(message) => Future.successful(Left(message))
      case Right((host, group, repo)) =>
        resolveParser(host) match {
          case Left(message) => Future.successful(Left(message))
          case Right(parser) =>
            handleResponse(client.committers(host, group, repo), List(200))(parser.authorToModel)
        }
    }
  }

  def commits(url: String): Future[Either[String, List[Commit]]] = {
    extract(url) match {
      case Left(message) => Future.successful(Left(message))
      case Right((host, group, repo)) =>
        resolveParser(host) match {
          case Left(message) => Future.successful(Left(message))
          case Right(parser) =>
            handleResponse(client.commits(host, group, repo), List(200))(parser.commitToModel)
        }
    }
  }

  def repoExists(url: String): Future[Either[String, Int]] = {
    extract(url) match {
      case Left(error) => Future.successful(Left(error))
      case Right((host, project, repo)) =>
        getHttpCode(client.repoExists(host, project, repo))
    }
  }

  def normalizeURL(url: String): Either[String, String] = {
    extract(url) match {
      case Left(error)                  => Left(error)
      case Right((host, project, repo)) => Right(client.normalize(host, project, repo))
    }
  }

   

  def handleResponse[A](futureResponse: Future[WSResponse], httpCodes: List[Int])(implicit parser: Parser[JsValue, Either[String, A]]): Future[Either[String, A]] = {
    onComplete(futureResponse)
    futureResponse.map { response =>
      if (!httpCodes.contains(response.status)) {
        logger.warn("Response: " + response.status + " => " + response.body)
        Left("Unexpected SCM response: " + response.status)
      } else {
        processResponse(parser(response.json))("Could not parse the json result(scm)!")
      }
    } recover {
      case e =>
        logger.error(e.getMessage)
        Left("An internal error occurred!")
    }
  }

  def onComplete[A](futureResponse: Future[WSResponse]) = {
    futureResponse onComplete {
      case Success(response) =>
      case Failure(t) => // Clients of this service don't need to know the details
        logger.error("Error while calling SCM " + t.getMessage)
    }
  }
  def getHttpCode(futureResponse: Future[WSResponse]): Future[Either[String, Int]] = {
    onComplete(futureResponse)
    futureResponse.map { response =>
      Right(response.status)
    } recover {
      case e =>
        logger.error(e.getMessage)
        Left("An internal error occurred!")
    }
  }

  def resolveParser(host: String): Either[String, SCMParser] =
    (GithubToJsonParser.resolve(host) orElse StashToJsonParser.resolve(host)) match {
      case Some(parser) => Right(parser)
      case None         => Left(s"Could not resolve the client for $host")
    }

  /**
   *
   * This method handles error messages such that sensitive message is not passed to the client.
   * I.e.: When authorization handshake fails, details shouldn't leave the service layer.
   *
   * @param Left the erroneous message and right the result.
   * @param errorMessage(implicit)
   * @return either a more general error left or the result right
   */
  private def processResponse[A](either: Either[String, A])(implicit errorMessage: String = "An internal error occurred!"): Either[String, A] = {
    either match {
      case Left(error) =>
        logger.warn(error)
        Left(errorMessage)
      case Right(result) => Right(result)
    }
  }

}