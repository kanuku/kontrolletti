package v1.util

import play.api.Logger

/**
 * Holds the regex expressions necessary for extracting
 * information from repo URL's of both github and stash.
 */
trait UrlParser {
  type HostProjectRepo = (String, String, String)
  //|/browse|/browse/|/browse/.*
  val repoSucceederRgx = """(/.*|.git)?"""
  val repoRgx = """(\w*(?!.git)*|[\w.-]*){1,1}"""
  val repoAntecedentRgx = """(/repos/|/){1,1}"""
  val projectRgx = """([\w-.]+){1,1}"""
  val projectAntecedentRgx = """(/projects/|/){1,1}"""
  val hostnameRgx = """(\w+[-.\w]*\w*[:\d]*){1,1}"""
  val userRgx = """(\w+@)?"""
  val protocolRgx = """(\w+://)?"""
  val urlRegex = s"$protocolRgx$userRgx$hostnameRgx$projectAntecedentRgx$projectRgx$repoAntecedentRgx$repoRgx$repoSucceederRgx".r
  /**
   * Extracts the host, project and repository from a repository-url of a github or stash project
   *
   *  @param url URL of the repository
   *  @return A reason why it could not parse OR the result (`host`, `project` and `repo`) extracted from the url.
   */
  def extract(url: String): Either[String, HostProjectRepo] = {
    Logger.info(s" Parsing $url ")
    url match {
      case ""   => Left("URL is empty")
      case null => Left("URL is null")
      case urlRegex(protocol, user, host, prjAntecedent, project, repoAntecedent, repo, succeeder) =>
        Right(host, project, repo)
      case _ =>
        Left(s"Could not parse $url")
    }
  }
}