package service

import scala.concurrent.Future
import model.Commit
import model.Repository
import model.Link
import model.Ticket
import com.google.inject.ImplementedBy

@ImplementedBy(classOf[SearchImpl])
trait Search {

  /**
   * Returns commits from the given repository in the project on the given host, optional ranges can be used to limit the result set.
   * @param host hostname/IP-address-address of the SCM server <br/>
   * @param project name of the project
   * @param repository name of the repository
   * @param since CommitId from where to start looking for commits
   * @param until Until commits from this commit
   * 
   * @return a future containing either the error(left) or list of commits(right)
   */
  def commits(host: String, project: String, repository: String, since: Option[String], until: Option[String]): Future[Either[String, Option[List[Commit]]]]

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

  /**
   * Fetches the tickets from.
   * @param host hostname/IP-address of the SCM server
   * @param project name of the project
   * @param repository name of the repository
   * @param since Includes tickets from this commit
   * @param until Until tickets from this commit
   * 
   * @return Either a Left with an error or a Right containing an Option with the List of found tickets or None if the repository does not exist(404).
   */
  def tickets(host: String, project: String, repository: String, since: Option[String], until: Option[String]): Future[Either[String, Option[List[Ticket]]]]
}

