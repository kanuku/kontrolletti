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
  val projectRgx = """([0-~-.]+){1,1}""" // inspired by ([ -~]+){1,1} - http://www.catonmat.net/blog/my-favorite-regex/ but from 0 (zero) to ~ (tilde) plus - (dash) and . (dot)
  val projectAntecedentRgx = """(/projects/|/scm/|/|:){1,1}"""
  val portRgx = """([:\d]*)?"""
  val hostnameRgx = """(\w+[-.\w]*\w*){1,1}"""
  val userRgx = """(\w+@)?"""
  val protocolRgx = """(\w+://)?"""
  val sshRgx = """(ssh://){1,1}"""
  val urlRegex = s"$protocolRgx$userRgx$hostnameRgx$portRgx$projectAntecedentRgx$projectRgx$repoAntecedentRgx$repoRgx$repoSucceederRgx".r
  val sshUrlRegex = s"$sshRgx$userRgx$hostnameRgx$portRgx$projectAntecedentRgx$projectRgx$repoAntecedentRgx$repoRgx$repoSucceederRgx".r

  /**
   * Extracts the `host`, `project` and `repo` from a repository-url of a github or stash project
   *
   *  @param url URL of the repository
   *  @return Either a [reason why it couldn't parse] left or a [result (`host`, `project` and `repo`)] right.
   */
  def extract(url: String): Either[String, HostProjectRepo] = {
    logger.debug(s" Parsing $url ")
    transform(url, extracter)
  }

  //General type for transformations
  type Transformer[A, B] = A => B

  type HostProjectRepo = (String, String, String)

  //Transforms  a partionedURL into a Tupple containing Host, Project and Repository
  private val extracter: Transformer[PartionedURL, HostProjectRepo] = { case PartionedURL(protocol, user, host, prjAntecedent, project, repoAntecedent, repo, succeeder) => (host, project, repo) }

  case class PartionedURL(protocol: String, user: String, host: String, prjAntecedent: String, project: String, repoAntecedent: String, repo: String, succeeder: String)

  private def transform[B](url: String, transformer: Transformer[PartionedURL, B]): Either[String, B] = url match {

    case link if (Option(link) == None || link.isEmpty()) =>
      Left("Repository-url should not be empty/null")

    case sshUrlRegex(protocol, user, host, port, prjAntecedent, project, repoAntecedent, repo, succeeder) =>
      logger.debug(s"SSH extract ($host, $project, $repo)")
      Right(transformer(PartionedURL(protocol, user, host, prjAntecedent, project, repoAntecedent, repo, succeeder)))
    case urlRegex(protocol, user, host, port, prjAntecedent, project, repoAntecedent, repo, succeeder) =>
      logger.debug(s"Regular extracted ($host, $project, $repo)")
      Right(transformer(PartionedURL(protocol, user, host + port, prjAntecedent, project, repoAntecedent, repo, succeeder)))
    case _ =>
      logger.warn(s"Could not parse $url")
      Left(s"Could not parse $url")

  }

}

//object test extends App {
//  val a = """(\w+[-.\w]*\w*){1,1}"""
//  val b = """([:\d]*)?"""
//  val pattern = s"$a$b".r
//  val result = "asdf" match {
//    case pattern(one, two) => one + "+" + two
//    case _                 => "Nothing"
//  }
//  println("Result >> " + result)
//}