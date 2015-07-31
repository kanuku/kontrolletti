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

/**
 * @author fbenjamin
 *
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
    resolveParser(host) match {
      case Right(scmParser) => handleRequest(scmParser.commitToModel, client.commits(host, project, repository, since, until))
      case Left(error)      => Future.successful(Left(error))

    }

  def commit(host: String, project: String, repository: String, id: String): Future[Either[String, Option[List[Commit]]]] =
    resolveParser(host) match {
      case Right(scmParser) => handleRequest(scmParser.commitToModel, client.commit(host, project, repository, id))
      case Left(error)      => Future.successful(Left(error))

    }

  def repos(host: String, project: String, repository: String): Future[Either[String, Option[List[Repository]]]] =
    resolveParser(host) match {
      case Right(scmParser) => handleRequest(scmParser.repoToModel, client.repos(host, project, repository))
      case Left(error)      => Future.successful(Left(error))

    }

  def parse(url: String): Either[String, (String, String, String)] = extract(url)

  def normalize(host: String, project: String, repository: String): String = client.repoUrl(host, project, repository)

  def isRepo(host: String, project: String, repository: String): Future[Either[String, Boolean]] = {
    val url = client.repoUrl(host, project, repository)
    isUrlValid(url)
  }

  def diff(host: String, project: String, repository: String, source: String, target: String): Future[Either[String, Option[Link]]] = {
    val url = client.diffUrl(host, project, repository, source, target)
    isUrlValid(url).map { response =>
      response.right.map {
        _ match {
          case true  => Some(new Link(url, null, null, null))
          case false => None
        }
      }

    }
  }
  def tickets(host: String, project: String, repository: String, since: Option[String], until: Option[String]): Future[Either[String, Option[List[Ticket]]]] =
    resolveParser(host) match {
      case Right(scmParser) => handleRequest(scmParser.ticketToModel, client.tickets(host, project, repository, since, until))
      case Left(error)      => Future.successful(Left(error))

    }

  def isUrlValid(url: String): Future[Either[String, Boolean]] =
    executeCall(client.head(url)).map { response =>
      response.right.map {
        _.status match {
          case status if ACCEPTABLE_CODES.contains(status) => true
          case 404                                         => false
        }
      }
    }

  /**
   * Handles calls to the client parses the jsonObject from the Response if necessary.
   * @param clientCall(param-by-name) the call to be executed in order to get the response.
   * @param parser the parser that transforms the jsonObjects(response) into the internal Model.
   * @return Either an Error-message(left) or the parsed Model(right).
   *
   */
  def handleRequest[A](parser: Parser[JsValue, Either[String, A]], clientCall: => Future[WSResponse]): Future[Either[String, Option[A]]] =
    executeCall(clientCall).map {
      _.right.flatMap { response =>
        response.status match {
          case 404 =>
            logger.info("Http code 404 (Does not exist)")
            Right(None)
          case status if (ACCEPTABLE_CODES.contains(status)) =>
            logger.info("Http code succefful does exist")
            Right(parser(response.json).right.toOption)
          case status =>
            logger.warn(s"Status $status was not hanled!")
            Left("Unexpected SCM response: " + response.status)
        }
      }
    }

  def logOnComplete(futureResponse: Future[WSResponse]) {
    futureResponse onComplete {
      case Success(response) => logger.info("Call succeed with http-status:" + response.status)
      case Failure(t)        => logger.error("Error while calling SCM " + t.getMessage)
    }
  }

  /**
   * Executes the call to the client and handles errors graciously.
   * @param call The call to be executed
   * @return EIther an error-message(left) or the Future-of-the–response(right) of the call.
   */
  def executeCall(call: => Future[WSResponse]): Future[Either[String, WSResponse]] = {
    Try(call) match {
      case Success(result) =>
        logOnComplete(result)
        result.map { Right(_) }
      case Failure(ex) =>
        logger.error(ex.getMessage)
        Future.successful(defaultError)
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