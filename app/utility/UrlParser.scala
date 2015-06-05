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

  //
  // The following general types for transformations were
  // created to avoid writing multiple transformation-methods

  //General type for transformations
  type Transformer[A, B] = A => B

  type HostProjectRepo = (String, String, String)

  // transforms a partionedURL into a partionedURL that can be used for stacking transformers
  val partionedURL: Transformer[PartionedURL, PartionedURL] = PartionedURL => PartionedURL

  type PartionedURL = (String, String, String, String, String, String, String, String)

  // transforms a partionedURL into a Normalized Github repository-URL
  val githubNormalizer: Transformer[PartionedURL, String] = { case (protocol, user, host, prjAntecedent, project, repoAntecedent, repo, succeeder) => s"https://$host/$project/$repo" }

  // transforms a partionedURL into a Normalized Stash repository-URL
  val stashNormalizer: Transformer[PartionedURL, String] = { case (protocol, user, host, prjAntecedent, project, repoAntecedent, repo, succeeder) => s"https://$host/projects/$project/repos/$repo/browse" }

  //Transforms  a partionedURL into a Tupple containing Host, Project and Repository
  val extracter: Transformer[PartionedURL, HostProjectRepo] = { case (protocol, user, host, prjAntecedent, project, repoAntecedent, repo, succeeder) => (host, project, repo) }

  private def transform[B](url: String, transformer: Transformer[PartionedURL, B]): Either[String, B] = url match {

    case link if (link == null || link.isEmpty()) =>
      Left("Repository-url should not be empty/null")

    case urlRegex(protocol, user, host, prjAntecedent, project, repoAntecedent, repo, succeeder) =>
      logger.info(s"Extracted ($host, $project, $repo)")
      Right(transformer(protocol, user, host, prjAntecedent, project, repoAntecedent, repo, succeeder))

    case _ =>
      logger.warn(s"Could not parse $url")
      Left(s"Could not parse $url")

  }

  /**
   * Tests if a string is empty after being trimmed
   * @param input - String to be tested
   * @return Boolean - if the String is null, empty or whitespaced-only
   */
  def empty(input: String): Boolean = { input == null || input.trim.isEmpty() }

  /**
   * A Stash urls can be identified in 3 manners if the regex expressions evaluate as following:<br>
   * 1. Protocol is `ssh://` <br>
   * 2. Protocol is `http*://`, project-antecedent = `/scm/`<br>
   * 3. Protocol is `http*://`, project-antecedent = `/projects/` and repoAntecedent = `/repos/`<br>
   */
  val isStash: Transformer[PartionedURL, Boolean] = {
    case ("ssh://", user, host, prjAntecedent, project, repoAntecedent, repo, succeeder) if (!empty(repo)) =>
      logger.info("Rule 1 - [ssh://] rule applied")
      true
    case ("https://", user, host, "/scm/", project, repoAntecedent, repo, succeeder) if (!empty(repo)) =>
      logger.info("Rule 2A - https://host[/scm/]* rule applied")
      true
    case ("http://", user, host, "/scm/", project, repoAntecedent, repo, succeeder) if (!empty(repo)) =>
      logger.info("Rule 2B - http://host[/scm/]* rule applied")
      true
    case ("https://", user, host, "/projects/", project, "/repos/", repo, succeeder) if (!empty(repo)) =>
      logger.info("Rule 3A - https://host[/projects/]*[/repos/]* rule applied")
      true
    case ("https://", user, host, "/projects/", project, "/repos/", repo, succeeder) if (!empty(repo)) =>
      logger.info("Rule 3B -  http://host[/projects/]*[/repos/]* rule applied")
      true
    case _ => false
  }

  /**
   * A Github url can be identified in 3 manners, if the regex expressions evaluate as following:<br>
   * 1. Host is `github.com`, project-antecedent = `/`<br>
   * 2. Project-antecedent = `:`, repo-antecedent = `/`<br>
   * 3. Protocol is `https://`, project-antecedent = `/` and repo-antecedent = `/`
   */
  def isGithub: Transformer[PartionedURL, Boolean] = {
    case (protocol, user, "github.com", prjAntecedent, project, repoAntecedent, repo, succeeder) if (!empty(repo)) =>
      logger.info("Rule 1 - [github.com]/project/repo rule applied")
      true
    case (protocol, user, host, ":", project, "/", repo, succeeder) if (!empty(repo)) =>
      logger.info("Rule 2 - https://github.com[:]zalando[/]kontrolletti.git")
      true
    case (protocol, user, host, "/", project, repoAntecedent, repo, succeeder) if (!empty(repo)) =>
      logger.info("Rule 2 - https://github.com[:]zalando/kontrolletti.git")
      true
    case _ => false
  }

  /**
   * Parses and returns the normalized URI for a github/stash repository-URL.
   * @param url url of the repository
   * @return either an error(left) or the normalized URI (right)
   */
  def normalize(url: String): Either[String, String] = {
    val result =    transform(url, partionedURL)
    result match {
      case Left(error) =>
        logger.info(error)
        Left(error)
      case Right(multiPart) =>
        logger.info(""+multiPart.toString())
        if (isStash(multiPart)) {
          logger.info("Normalizing to github-repo-url")
          transform(url, stashNormalizer)
        } else if (isGithub(multiPart)) {
          logger.info("Normalizing to stash-repo-url")
          transform(url, githubNormalizer)
        } else {
          logger.info("Categorize if URL represents a Stash/Github repository")
          Left("Categorize if URL represents a Stash/Github repository")
        }
      case _ => 
            logger.info("Categorize if URL represents a Stash/Github repository")
        Left("Something went really wrong now!!")
    }
  }

}