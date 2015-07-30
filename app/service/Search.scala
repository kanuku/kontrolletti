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
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSResponse
import client.GithubToJsonParser
import client.StashToJsonParser
import client.SCMParser
import client.SCMParser
import client.SCMResolver
import model.Commit
import utility.UrlParser
import model.Repository
import model.Link
import model.Ticket
import scala.util.Try

trait Search {

  /**
   * Returns commits from the given repository in the project on the given host, optional ranges can be used to limit the result set.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repository name of the repository
   * @param since CommitId from where to start looking for commits
   * @param until Until commits from this commit
   * @return a future containing either the error(left) or list of commits(right)
   */
  def commits(host: String, project: String, repository: String, since: Option[String], until: Option[String]): Future[Either[String, Option[List[Commit]]]]

  /**
   * Returns a single commit from the given repository in the project on the given host.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repository name of the repository
   * @param id commitId to be returned
   *
   * @return a future containing either the error(left) or list of commits(right)
   */
  def commit(host: String, project: String, repository: String, id: String): Future[Either[String, Option[List[Commit]]]]

  /**
   * Returns repositories from the given repository in the project on the given host.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repository name of the repository
   * @return a future containing either the error(left) or list of commits(right)
   */
  def repos(host: String, project: String, repository: String): Future[Either[String, Option[List[Repository]]]]

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
   * @param repository name of the repository
   * @return either an error(left) or the normalized URI (right)
   */
  def normalize(host: String, project: String, repository: String): String

  /**
   * Checks if the repository exists by sending a HEAD request to the original repository link.
   * @param host DNS/IP of the SCM server
   * @param project name of the project
   * @param repository name of the repository
   * @return  Either an Left with an error or a Right(true) if the HTTP-CODE returned is 200/301 and Right(false) if (404).
   */
  def isRepo(host: String, project: String, repository: String): Future[Either[String, Boolean]]

  /**
   * Checks if the diff exists by sending a HEAD request to the SCM server.
   * @param host DNS/IP of the SCM server
   * @param project name of the project
   * @param repository name of the repository
   * @param source commit-id from where to compare from
   * @param target commit-id from where to compare to
   * @return  Either an Left with an error or a Right(true) if the HTTP-CODE returned is 200/301 and Right(false) if (404).
   */
  def diff(host: String, project: String, repository: String, source: String, target: String): Future[Either[String, Option[Link]]]

  /**
   * Fetches the tickets from.
   * @param host DNS/IP of the SCM server
   * @param project name of the project
   * @param repository name of the repository
   * @param since Includes tickets from this commit
   * @param until Until tickets from this commit
   * @return Either a Left with an error or a Right containing an Option with the List of found tickets or None if the repository does not exist(404).
   */
  def tickets(host: String, project: String, repository: String, since: Option[String], until: Option[String]): Future[Either[String, Option[List[Ticket]]]]
}

/**
 * This class handles the search logic and retrieves the data from
 * the right source (Stash/Github).
 *
 */
@Singleton
class SearchImpl @Inject() (client: SCM) extends Search with UrlParser {

  type Parser[JsValue, B] = JsValue => B

  private val logger: Logger = Logger(this.getClass())
  private val defaultError = Left("Something went wrong, check the logs!")
  private val ACCEPTABLE_CODES = List(200)

  def commits(host: String, project: String, repository: String, since: Option[String], until: Option[String]): Future[Either[String, Option[List[Commit]]]] =
    Future {
      lazy val call = client.commits(host, project, repository, since, until)
      resolveParser(host).right.flatMap(parser => handleRequest(parser.commitToModel, call))
    }

  def commit(host: String, project: String, repository: String, id: String): Future[Either[String, Option[List[Commit]]]] =
    Future {
      lazy val call = client.commit(host, project, repository, id)
      resolveParser(host).right.flatMap(parser => handleRequest(parser.commitToModel, call))
    }

  def repos(host: String, project: String, repository: String): Future[Either[String, Option[List[Repository]]]] =
    Future {
      lazy val call = client.repos(host, project, repository)
      resolveParser(host).right.flatMap(parser => handleRequest(parser.repoToModel, call))

    }

  def parse(url: String): Either[String, (String, String, String)] = extract(url)

  def normalize(host: String, project: String, repository: String): String = client.url(host, project, repository)

  def isRepo(host: String, project: String, repository: String): Future[Either[String, Boolean]] = Future { ??? }

  def diff(host: String, project: String, repository: String, source: String, target: String): Future[Either[String, Option[Link]]] = Future { ??? }

  def tickets(host: String, project: String, repository: String, since: Option[String], until: Option[String]): Future[Either[String, Option[List[Ticket]]]] = {
      lazy val call = client.tickets(host, project, repository, since, until)
      resolveParser(host) match {
      case Right(scmParser) => handleRequest(scmParser.ticketToModel, call)
      case Left(error)      => Future.successful(Left(error))

    } 
  }

  /**
   * Handles calls to the client parses the jsonObject from the Response if neccessary.
   * @param clientCall(param-by-name) the call to be executed in order to get the response.
   * @param parser the parser that transforms the jsonObjects(response) into the internal Model.
   * @return Either an Error-message(left) or the parsed Model(right).
   *
   */
  //  private def handleRequest[A](parser: Parser[JsValue, Either[String, A]], clientCall: => Future[WSResponse]): Either[String, Option[A]] = {
  //    executeCall(clientCall).right.flatMap { fResponse =>
  //      val result = unwrapResponse(fResponse)
  //      result.flatMap { wsResponse =>
  //        wsResponse.right match {
  //          case None           => Right(None) //In case is 404 return None 
  //          case Some(response) => Right(parser(response.json).right.toOption)
  //        }
  //      }
  //    }
  //  }

  def handleRequest[A](parser: Parser[JsValue, Either[String, A]], clientCall: => Future[WSResponse]): Future[Either[String, Option[A]]] = {
    executeCall(clientCall) match {
      case Left(error) => Future.successful(Left(error))
      case Right(futureResponse) => unwrapResponse(futureResponse).map { unwrappedResponse =>
        unwrappedResponse match {
          case Right(response) => response match {
            case None           => Right(None)
            case Some(response) => Right(parser(response.json).right.toOption)
          }
          case Left(error) => Left(error)
        }
      }
    }
  }
  /**
   * Unwraps the response and transforms it, based on their http-status-codes, to a usable response in this service.
   * For now we are interested in responses that have parseable payload and return successfull http-status-codes.
   * @param futureResponse Future of the WSResponse
   * @return Either a Left with an error or a Right containing an Optional(Response=Success, None=404).
   *
   */
  def unwrapResponse(futureResponse: Future[WSResponse]): Future[Either[String, Option[WSResponse]]] = {
    futureResponse onComplete {
      case Success(response) => logger.info("Call succeed ")
      case Failure(t)        => logger.error("Error while calling SCM " + t.getMessage)
    }
    var result: Either[String, Option[WSResponse]] = Left("OMG!! This should never happen!!")
    futureResponse.map { response =>
      logger.info("Response with http-status-code: " + response.status)
      response.status match {
        case 404 =>
          logger.info("Http code 404 (Does not exist)")
          Right(None)
        case status if (ACCEPTABLE_CODES.contains(status)) =>
          logger.info("Http code succefful does exist")
          Right(Some(response))
        case status =>
          logger.warn(s"Status $status was not hanled!")
          Left("Unexpected SCM response: " + response.status)
      }
    }
  }

  /**
   * Executes the call to the client and handles errors graciously.
   * @param call The call to be executed
   * @return EIther an error-message(left) or the Future-of-the–response(right) of the call.
   */
  def executeCall(call: => Future[WSResponse]): Either[String, Future[WSResponse]] = {
    Try(call) match {
      case Success(result) => Right(result)
      case Failure(ex) =>
        logger.error(ex.getMessage)
        defaultError
    }
  }

  /**
   * Utility method to find the right parser for the right SCM.
   * @param host DNS/IP of the SCM server
   * @return EIther an error-message(left) or the found SCM-parser(right).
   *
   */
  private def resolveParser(host: String): Either[String, SCMParser] =
    (GithubToJsonParser.resolve(host) orElse StashToJsonParser.resolve(host)) match {
      case Some(parser) => Right(parser)
      case None         => Left(s"Could not resolve the client for $host")
    }

}