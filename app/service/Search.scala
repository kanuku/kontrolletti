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
import model.Repository
import model.Link
import model.Ticket

trait Search {

  /**
   * Returns committers from the given repository in the project on the given host.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @return a future containing either the error(left) or list of commits(right)
   */
  def committers(host: String, project: String, repo: String): Future[Either[String, List[Author]]]
  /**
   * Returns commits from the given repository in the project on the given host.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @return a future containing either the error(left) or list of commits(right)
   */
  def commits(host: String, project: String, repo: String): Future[Either[String, List[Commit]]]

  /**
   * Returns repositories from the given repository in the project on the given host.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @return a future containing either the error(left) or list of commits(right)
   */
  def repos(host: String, project: String, repo: String): Future[Either[String, List[Repository]]]

  /**
   * Parse a url into 3 separate parameters, the `host`, `project` and `repo` from a repository-url of a github or stash project
   *
   *  @param url URL of the repository
   *  @return Either a [reason why it couldn't parse] left or a [result (`host`, `project` and `repo`)] right.
   */
  def parse(url: String): Either[String, (String, String, String)]

  /**
   * Parses and returns the normalized URI for a github/stash repository-URL.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repo name of the repository
   * @return either an error(left) or the normalized URI (right)
   */
  def normalize(host: String, project: String, repo: String): String

  /**
   * Validates the repository by sending a HEAD request to the original repository link.
   * @param host DNS/IP of the SCM server
   * @param project name of the project
   * @param repo name of the repository
   * @return  Either an Left with an error or a Right, true if the HTTP-CODE returned is 200/301 and false otherwise.
   */
  def repoExists(host: String, project: String, repo: String): Future[Either[String, Boolean]]
  
  /**
   * Validates the diff by sending a HEAD request to the original diff link.
   * @param host DNS/IP of the SCM server
   * @param project name of the project
   * @param repo name of the repository
   * @param sourceId commit-id from where to compare from
   * @param targetId commit-id from where to compare to
   * @return Either an Left with an error or a Right with a link if the returned HTTP-CODE is 200/301 or a None otherwise.
   */
  def diffExists(host: String, project: String, repo: String, sourceId:String, targetId:String): Future[Either[String, Option[Link]]]

  /**
   * Fetches the tickets from.
   * @param host DNS/IP of the SCM server
   * @param project name of the project
   * @param repo name of the repository
   * @param sinceId Includes tickets from this commit
   * @param untilId Until tickets from this commit 
   * @return Either an Left with an error or a Right with list of found tickets.
   */  
  def tickets(host: String, project: String, repo: String, sinceId:String, untilId:String): Future[Either[String, Option[List[Ticket]]]]
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
  private val ACCEPTABLE_CODES = List(200, 301)
  def parse(url: String): Either[String, (String, String, String)] = extract(url)

  def committers(host: String, project: String, repo: String): Future[Either[String, List[Author]]] = {
    resolveParser(host) match {
      case Left(message) => Future.successful(Left(message))
      case Right(parser) =>
        handleResponse(client.committers(host, project, repo), List(200))(parser.authorToModel)
    }
  }

  def commits(host: String, project: String, repo: String): Future[Either[String, List[Commit]]] = {
    resolveParser(host) match {
      case Left(message) => Future.successful(Left(message))
      case Right(parser) =>
        handleResponse(client.commits(host, project, repo), List(200))(parser.commitToModel)
    }
  }

  def repos(host: String, project: String, repo: String): Future[Either[String, List[Repository]]] = ???

  def repoExists(host: String, project: String, repo: String): Future[Either[String, Boolean]] = {
    val futureResponse = client.repoExists(host, project, repo)
    onComplete(futureResponse)
    futureResponse.map { response =>
      Right(ACCEPTABLE_CODES.contains(response.status))
    } recover {
      case e =>
        logger.error(e.getMessage)
        Left("An internal error occurred!")
    }

  }

  def diffExists(host: String, project: String, repo: String, sourceId:String, targetId:String): Future[Either[String, Option[Link]]]= ???

  def tickets(host: String, project: String, repo: String, sinceId:String, untilId:String): Future[Either[String, Option[List[Ticket]]]] = ???
  
  def normalize(host: String, project: String, repo: String): String = {
    client.url(host, project, repo)
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