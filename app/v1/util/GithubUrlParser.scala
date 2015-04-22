package v1.util

import play.api.Logger

/**
 * An utility trait
 *
 */
trait GithubUrlParser {

  //catches=>https://git-hub.com:8080/zalando-bus/kontrolletti/
  private val regex01 = """(\w+)://(\w+[\-.]*\w+[\-.]*\w+):(\d+)/(\w+[\-_]*\w+)/(\w+[\-._]*\w+)/""".r

  //catches=>https://git-hub.com:8080/zalando-bus/kontrolletti
  private val regex02 = """(\w+)://(\w+[\-.]*\w+[\-.]*\w+):(\d+)/(\w+[\-_]*\w+)/(\w+[\-._]*\w+)""".r

  //catches=>ssh://git@git-hub.com:22/zalando-bus/kontrolletti.git
  private val regex03 = """(\w+)://(\w+[\@]*)(\w+[\-.]*\w+[\-.]*\w+):(\d+)/(\w+[\-_]*\w+)/(\w+[\-._]*\w+)""".r

  //catches=>git@git-hub.com:22/zalando-bus/kontrolletti.git
  private val regex04 = """(\w+[\@]*)(\w+[\-.]*\w+[\-.]*\w+):(\d+)/(\w+[\-_]*\w+)/(\w+[\-._]*\w+).git""".r

  //catches=>git@git-hub.com:zalando-bus/kontrolletti.git
  private val regex05 = """(\w+[\@]*)(\w+[\-.]*\w+[\-.]*\w+):(\w+[\-_]*\w+)/(\w+[\-._]*\w+).git""".r

  //catches=>https://github.com/zalando-bus/kontrolletti/
  private val regex06 = """(\w+)://(\w+[\-.]*\w+[\-.]*\w+)/(\w+[\-_]*\w+)/(\w+[\-_]*\w+)/""".r

  //catches=>https://github.com/zalando-bus/kontrolletti
  private val regex07 = """(\w+)://(\w+[\-.]*\w+[\-.]*\w+)/(\w+[\-_]*\w+)/(\w+[\-_]*\w+)""".r

  //catches=>github.com/zalando-bus/kontrolletti/
  private val regex08 = """(\w+[\-.]*\w+[\-.]*\w+)/(\w+[\-_]*\w+)/(\w+[\-_]*\w+)/""".r

  //catches=>github.com/zalando-bus/kontrolletti
  private val regex09 = """(\w+[\-.]*\w+[\-.]*\w+)/(\w+[\-_]*\w+)/(\w+[\-_]*\w+)""".r

  /**
   * Splits the repository-url of a github project into three parts:
   *  Host , Group(user/org) , Repo(project)
   *
   *
   *  @param url the url to be split
   *  @return the `host`, the `group` and the `repo`
   */
  def parse(url: String): (String, String, String) = {
    url match {
      case regex01(protocol, host, port, group, repo) =>
        Logger.debug(s"$protocol - $host - $port - $group - $repo")
        (host, group, repo)
      case regex02(protocol, host, port, group, repo) =>
        Logger.debug(s"$protocol - $host - $port - $group - $repo")
        (host, group, repo)
      case regex03(protocol, user, host, port, group, repo) =>
        Logger.debug(s"$protocol - $user - $host - $port - $group - $repo")
        (host, group, repo)
      case regex04(user, host, port, group, repo) =>
        Logger.debug(s"$user - $host - $port - $group - $repo")
        (host, group, repo)
      case regex05(user, host, group, repo) =>
        Logger.debug(s"$user - $host - $group - $repo")
        (host, group, repo)
      case regex06(protocol, host, group, repo) =>
        Logger.debug(s"$protocol - $host - $group - $repo")
        (host, group, repo)
      case regex07(protocol, host, group, repo) =>
        Logger.debug(s"$protocol - $host - $group - $repo")
        (host, group, repo)
      case regex08(host, group, repo) =>
        Logger.debug(s"$host - $group - $repo")
        (host, group, repo)
      case regex09(host, group, repo) =>
        Logger.debug(s"$host - $group - $repo  ")
        (host, group, repo)
      case _ =>
        Logger.debug(s"Could not parse the $url")
        ("", "", "")
    }
  }
}