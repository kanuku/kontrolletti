package v1.util

import play.api.Logger

/**
 * An utility trait
 *
 */
trait UrlParser {

  val hostname = """(\w+[-.\w*]*\w*[:\d]*)+"""
  val protocol = """(\w+://)*"""
  val user = """(\w+@)*"""
  val projectLink ="""(/projects)*"""
  val regex01 = s"($hostname)+".r
  val regex02 = s"$protocol$user$hostname".r

  /**
   * Splits the repository-url of a github project into three parts:
   *  Host , project(user/org) , repository
   *
   *  @param url the url to be split
   *  @return the `host`, the `project` and the `repo`
   */
  def parse(url: String): (String, String, String) = {
    url match {
      case regex02(protocol, user, host) =>
        Logger.error(s"regex01 $url => protocol($protocol) user($user) host(host) ")
        (host, null, null)
      //      case regex01(protocol, host, port, group, repo) =>
      //        Logger.debug(s"regex01 $protocol - $host - $port - $group - $repo")
      //        (host, group, repo)
      case _ =>
        Logger.error(s"Could not parse $url")
        ("", "", "")
    }
  }
}

object A extends App with UrlParser {
  val users = new UrlParserTest().fixture.protocolWithUser
  for (protocol <- users) {
    println(parse(protocol))
  }
}