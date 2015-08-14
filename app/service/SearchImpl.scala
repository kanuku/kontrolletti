package service

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import akka.dispatch.OnComplete
import akka.dispatch.OnFailure
import client.GithubToJsonParser
import client.scm.SCM
import client.SCMParser
import client.SCMParser
import client.StashToJsonParser
import javax.inject._
import model.Commit
import model.Link
import model.Repository
import model.Ticket
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.ws.WS
import play.api.libs.ws.WSResponse
import utility.UrlParser

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
  private val acceptableCodes = List(200)

  def commits(host: String, project: String, repository: String, since: Option[String], until: Option[String]): Future[Either[String, Option[List[Commit]]]] =
    resolveParser(host) match {
      case Right(scmParser) => handleRequest(scmParser.commitToModel, client.commits(host, project, repository, since, until))
      case Left(error)      => Future.successful(Left(error))
    }

  def commit(host: String, project: String, repository: String, id: String): Future[Either[String, Option[Commit]]] =
    resolveParser(host) match {
      case Right(scmParser) => handleRequest(scmParser.singleCommitToModel, client.commit(host, project, repository, id))
      case Left(error)      => Future.successful(Left(error))
    }

  def repo(host: String, project: String, repository: String): Future[Either[String, Option[Repository]]] =
    resolveParser(host) match {
      case Right(scmParser) => handleRequest(scmParser.repoToModel, client.repo(host, project, repository))
      case Left(error)      => Future.successful(Left(error))
    }

  def parse(url: String): Either[String, (String, String, String)] = extract(url)

  def normalize(host: String, project: String, repository: String): String = client.repoUrl(host, project, repository)

  def isRepo(host: String, project: String, repository: String): Future[Either[String, Boolean]] = {
    val url = client.repoUrl(host, project, repository)
    isUrlValid(host, url)
  }

  def diff(host: String, project: String, repository: String, source: String, target: String): Future[Either[String, Option[Link]]] = {
    val url = client.diffUrl(host, project, repository, source, target)
    isUrlValid(host, url).map { response =>
      response.right.map {
        _ match {
          case true  => Option(new Link(url, null, null, null))
          case false => None
        }
      }
    }
  }
  def tickets(host: String, project: String, repository: String, since: Option[String], until: Option[String]): Future[Either[String, Option[List[Ticket]]]] =
    resolveParser(host) match {
      //TODO This should go to our local datastore
      case Right(scmParser) => handleRequest(scmParser.ticketToModel, client.tickets(host, project, repository))
      case Left(error)      => Future.successful(Left(error))
    }

  def isUrlValid(host: String, url: String): Future[Either[String, Boolean]] =
    executeCall(client.head(host, url)).map { response =>
      response.right.map {
        _.status match {
          case status if acceptableCodes.contains(status) => true
          case _ => false
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
          case status if (acceptableCodes.contains(status)) =>
            logger.info("Http code succefful")
            parser(response.json) match {
              case Right(value) =>
                Right(Some(value))
              case Left(error) =>
                Logger.error(error)
                Left(error)
            }
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
   * Utility method to find the right parser for the given host (SCM).
   * @param host hostname/IP-address of the SCM server
   * @return EIther an error-message(left) or the found SCM-parser(right).
   *
   */
  private def resolveParser(host: String): Either[String, SCMParser] =
    (GithubToJsonParser.resolve(host) orElse StashToJsonParser.resolve(host)) match {
      case Some(parser) => Right(parser)
      case None         => Left(s"Could not resolve the client for $host")
    }

}


