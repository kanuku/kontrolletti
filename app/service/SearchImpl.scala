package service

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import client.scm.GithubToJsonParser
import client.scm.SCM
import client.scm.SCMParser
import client.scm.SCMParser
import client.scm.StashToJsonParser
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
import play.api.libs.ws.WSResponse
import utility.UrlParser
import scala.Left
import scala.Right
import org.joda.time.format.DateTimeFormat

/**
 * @author fbenjamin
 *
 * This class handles the search logic and retrieves the data from
 * the right source (Stash/Github).
 *
 */

@Singleton
class SearchImpl @Inject() (client: SCM) extends Search with UrlParser {

  type Parser[B] = JsValue => B

  private val logger: Logger = Logger(this.getClass())
  private val defaultError = Left("Something went wrong, check the logs!")
  private val acceptableCodes = List(200)
  private val githubDateParser = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");

  def commits(host: String, project: String, repository: String, since: Option[Commit], until: Option[Commit], pageNr: Int): Future[Either[String, Option[List[Commit]]]] = {
    logger.info(s"commits: $host - $project - $repository - $pageNr")
    resolveParser(host) match {
      case Right(scmParser) =>
        val sinceParam = limitCommits(host, since)
        val untilParam = limitCommits(host, until)
        handleRequest(scmParser.commitToModel, client.commits(host, project, repository, sinceParam, untilParam, pageNr))
      case Left(error) => Future.successful(Left(error))
    }
  }

  def limitCommits(host: String, since: Option[Commit]): Option[String] = since match {
    case Some(commit) => if (client.isGithubServerType(host))
      Some(githubDateParser.print(commit.date))
    else
      Some(commit.id)
    case None => None
  }

  def commit(host: String, project: String, repository: String, id: String): Future[Either[String, Option[Commit]]] = {
    logger.info(s"commit: $host - $project - $repository - $id")
    resolveParser(host) match {
      case Right(scmParser) => handleRequest(scmParser.singleCommitToModel, client.commit(host, project, repository, id))
      case Left(error)      => Future.successful(Left(error))
    }
  }

  def repo(host: String, project: String, repository: String): Future[Either[String, Option[Repository]]] = {
    logger.info(s"repo: $host - $project - $repository")
    resolveParser(host) match {
      case Right(scmParser) => handleRequest(scmParser.repoToModel, client.repo(host, project, repository))
      case Left(error)      => Future.successful(Left(error))
    }
  }

  def parse(url: String): Either[String, (String, String, String)] = extract(url)

  def normalize(host: String, project: String, repository: String): String = {
    logger.info(s"normalize: $host - $project - $repository")
    client.repoUrl(host, project, repository)
  }

  def isRepo(host: String, project: String, repository: String): Future[Either[String, Boolean]] = {
    logger.info(s"isRepo: $host - $project - $repository")
    val url = client.repoUrl(host, project, repository)
    isUrlValid(host, url)
  }

  def diff(host: String, project: String, repository: String, source: String, target: String): Future[Either[String, Option[Link]]] = {
    logger.info(s"diff: $host - $project - $repository - $source - $target")
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
  def tickets(host: String, project: String, repository: String, since: Option[String], until: Option[String]): Future[Either[String, Option[List[Ticket]]]] = {
    logger.info(s"tickets: $host - $project - $repository - $since - $until")
    resolveParser(host) match {
      //TODO This should go to our local datastore
      case Right(scmParser) => handleRequest(scmParser.ticketToModel, client.tickets(host, project, repository))
      case Left(error)      => Future.successful(Left(error))
    }
  }

  def isUrlValid(host: String, url: String): Future[Either[String, Boolean]] = {
    logger.info(s"isUrlValid: $host - $url")
    executeCall(client.head(host, url)).map { response =>
      response.right.map {
        _.status match {
          case status if acceptableCodes.contains(status) => true
          case _ => false
        }
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
  def handleRequest[A](parser: Parser[Either[String, A]], clientCall: => Future[WSResponse]): Future[Either[String, Option[A]]] =
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
            logger.warn(s"Status $status was not handled! ->" + response.body)
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
