package service

import scala.{ Left, Right }
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }
import org.joda.time.format.DateTimeFormat
import com.google.inject.ImplementedBy
import client.scm.{ GithubToJsonParser, SCM, SCMImpl, SCMParser, StashToJsonParser }
import javax.inject.{ Inject, Singleton }
import model.{ Commit, Link, Repository, Ticket }
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse
import utility.UrlParser
import configuration.SCMConfiguration

trait Search {

  /**
   * Returns commits from the given repository in the project on the given host, optional ranges can be used to limit the result set.
   * @param host hostname/IP-address-address of the SCM server <br/>
   * @param project name of the project
   * @param repository name of the repository
   * @param since commits made after this Commit
   * @param until Until this commit
   * @param pageNr number of the page to retrieve
   * @return a future containing either the error(left) or list of commits(right)
   */
  def commits(host: String, project: String, repository: String, since: Option[Commit], until: Option[Commit], pageNr: Int): Future[Either[String, Option[List[Commit]]]]

  /**
   * Returns a single commit from the given repository in the project on the given host.
   * @param host hostname/IP-address of the SCM server <br/>
   * @param project name of the project
   * @param repository name of the repository
   * @param id commit-id to be returned
   *
   * @return a future containing either the error(left) or list of commits(right)
   */
  def commit(host: String, project: String, repository: String, id: String): Future[Either[String, Option[Commit]]]

  /**
   * Returns a single repository from the given project on the given host.
   * @param host hostname/IP-address of the SCM server <br/>
   * @param project name of the project
   * @param repository name of the repository
   *
   * @return a future containing either the error(left) or list of commits(right)
   */
  def repo(host: String, project: String, repository: String): Future[Either[String, Option[Repository]]]

  /**
   * Parse a url into 3 separate parameters, the `host`, `project` and `repo` from a repository-url of a github or stash project
   *
   *  @param url URL of the repository
   *
   *  @return Either a [reason why it couldn't parse] left or a [result (`host`, `project` and `repo`)] right.
   */
  def parse(url: String): Either[String, (String, String, String)]

  /**
   * Parses and returns the normalized URI for a github/stash repository-URL.
   * @param host hostname/IP-address of the SCM server <br/>
   * @param project name of the project
   * @param repository name of the repository
   *
   * @return either an error(left) or the normalized URI (right)
   */
  def normalize(host: String, project: String, repository: String): String

  /**
   * Checks if the repository exists by sending a HEAD request to the original repository link.
   * @param host hostname/IP-address of the SCM server
   * @param project name of the project
   * @param repository name of the repository
   *
   * @return  Either an Left with an error or a Right(true) if the HTTP-CODE returned is 200/301 and Right(false) if (404).
   */
  def isRepo(host: String, project: String, repository: String): Future[Either[String, Boolean]]

  /**
   * Creates a diff-url and checks if the url exists.
   * @param host hostname/IP-address of the SCM server
   * @param project name of the project
   * @param repository name of the repository
   * @param source commit-id from where to compare from
   * @param target commit-id from where to compare to
   *
   * @return  Either an Left with an error or a Right(true) if the HTTP-CODE returned is 200/301 and Right(false) if (404).
   */
  def diff(host: String, project: String, repository: String, source: String, target: String): Future[Either[String, Option[Link]]]

}

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
    lazy val call = client.resolver(host).allowedProjects(host)
    Try(call.toList) match {
      case Success(Nil) =>
        isUrlValid(host, client.repoUrl(host, project, repository))
      case Success(allowedProjects) if allowedProjects.contains(project.toLowerCase()) =>
        isUrlValid(host, client.repoUrl(host, project, repository))
      case Success(allowedProjects) =>
        Future.successful(Right(false))
      case Failure(ex) =>
        logger.error("An error occurred:" + ex.getMessage)
        Future.successful(defaultError)
    }
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
