package utility

import play.api.Logger

/**
 * Holds the regex expressions necessary for extracting
 * information from repo URL's of both github and stash.
 */
trait UrlParser {

  private val logger: Logger = Logger(this.getClass())

  /**
   * The link gets split up into multiple parts
   */
  val repoSucceederRgx = """(/.*|.git)?"""
  val repoRgx = """([\w.-]*?){1,1}"""
  val repoAntecedentRgx = """(/repos/|/){1,1}"""
  val projectRgx = """([\w-.]+){1,1}"""
  val projectAntecedentRgx = """(/projects/|/scm/|/|:){1,1}"""
  val hostnameRgx = """(\w+[-.\w]*\w*[:\d]*){1,1}"""
  val userRgx = """(\w+@)?"""
  val protocolRgx = """(\w+://)?"""
  val urlRegex = s"$protocolRgx$userRgx$hostnameRgx$projectAntecedentRgx$projectRgx$repoAntecedentRgx$repoRgx$repoSucceederRgx".r

  /**
   * Extracts the `host`, `project` and `repo` from a repository-url of a github or stash project
   *
   *  @param url URL of the repository
   *  @return Either a [reason why it couldn't parse] left or a [result (`host`, `project` and `repo`)] right.
   */
  def extract(url: String): Either[String, HostProjectRepo] = {
    logger.info(s" Parsing $url ")
    transform(url, extracter)
  }

 
  //General type for transformations
  type Transformer[A, B] = A => B

  type HostProjectRepo = (String, String, String)

  //Transforms  a partionedURL into a Tupple containing Host, Project and Repository
  private val extracter: Transformer[PartionedURL, HostProjectRepo] = { case (protocol, user, host, prjAntecedent, project, repoAntecedent, repo, succeeder) => (host, project, repo) }

  type PartionedURL = (String, String, String, String, String, String, String, String)

  private def transform[B](url: String, transformer: Transformer[PartionedURL, B]): Either[String, B] = url match {

    case link if (Option(link) == None || link.isEmpty()) =>
      Left("Repository-url should not be empty/null")

    case urlRegex(protocol, user, host, prjAntecedent, project, repoAntecedent, repo, succeeder) =>
      logger.info(s"Extracted ($host, $project, $repo)")
      Right(transformer(protocol, user, host, prjAntecedent, project, repoAntecedent, repo, succeeder))

    case _ =>
      logger.warn(s"Could not parse $url")
      Left(s"Could not parse $url")

  }

}