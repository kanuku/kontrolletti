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
  def commit(host: String, project: String, repository: String, id: String): Future[Either[String, Option[Commit]]]

  /**
   * Returns repositories from the given repository in the project on the given host.
   * @param host DNS/IP of the SCM server <br/>
   * @param project name of the project
   * @param repository name of the repository
   * @return a future containing either the error(left) or list of commits(right)
   */
  def repos(host: String, project: String, repository: String): Future[Either[String, List[Repository]]]

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
   * Validates the repository by sending a HEAD request to the original repository link.
   * @param host DNS/IP of the SCM server
   * @param project name of the project
   * @param repository name of the repository
   * @return  Either an Left with an error or a Right, true if the HTTP-CODE returned is 200/301 and false otherwise.
   */
  def repoExists(host: String, project: String, repository: String): Future[Either[String, Boolean]]

  /**
   * Validates the diff by sending a HEAD request to the original diff link.
   * @param host DNS/IP of the SCM server
   * @param project name of the project
   * @param repository name of the repository
   * @param source commit-id from where to compare from
   * @param target commit-id from where to compare to
   * @return Either an Left with an error or a Right with a link if the returned HTTP-CODE is 200/301 or a None otherwise.
   */
  def diffExists(host: String, project: String, repository: String, source: String, target: String): Future[Either[String, Option[Link]]]

  /**
   * Fetches the tickets from.
   * @param host DNS/IP of the SCM server
   * @param project name of the project
   * @param repository name of the repository
   * @param since Includes tickets from this commit
   * @param until Until tickets from this commit
   * @return Either an Left with an error or a Right with list of found tickets.
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

  def commits(host: String, project: String, repository: String, since: Option[String], until: Option[String]): Future[Either[String, Option[List[Commit]]]] = ???
  
  def commit(host: String, project: String, repository: String, id: String): Future[Either[String, Option[Commit]]] = ???

  def repos(host: String, project: String, repository: String): Future[Either[String, List[Repository]]] = ???

  def parse(url: String): Either[String, (String, String, String)] = ???

  def normalize(host: String, project: String, repository: String): String = ???

  def repoExists(host: String, project: String, repository: String): Future[Either[String, Boolean]] = ???

  def diffExists(host: String, project: String, repository: String, source: String, target: String): Future[Either[String, Option[Link]]] = ???

  def tickets(host: String, project: String, repository: String, since: Option[String], until: Option[String]): Future[Either[String, Option[List[Ticket]]]] = ???

}