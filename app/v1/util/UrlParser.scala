package v1.util

import play.api.Logger

/**
 * This trait contains the regex parse-expressions for extracting
 * necessary information from repo URL's from github and stash.
 *
 *
 */
trait UrlParser {
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
   * Splits the repository-url of a github project into three parts:
   *  Host , project(user/org) , repository
   *
   *  @param url the url to be split
   *  @return the `host`, the `project` and the `repo`
   */
  def parse(url: String): (String, String, String) = {
    Logger.error(s" #### $url ")
    url match {
      case "" =>
        Logger.info(s"URL is empty")
        ("", "", "")
      case null =>
        Logger.info(s"URL is null")
        ("", "", "")
      case urlRegex(protocol, user, host, prjAntecedent, project, repoAntecedent, repo, succeeder) =>
        Logger.error(s"urlRegex $url => protocol($protocol) user($user) host(host) antecedent($prjAntecedent)"
          + " repo($repo) succeeder($succeeder)")
        (host, project, repo)
      case _ =>
        Logger.error(s"Could not parse $url")
        ("", "", "")
    }
  }
}
//object A extends App with UrlParser {
//  import v1.test.util.ParsingData
//    val regex =  s"$protocolRgx$userRgx$hostnameRgx$projectAntecedentRgx$projectRgx$repoAntecedentRgx$repoRgx$repoSucceederRgx".r
//    val value =  "git@git-hub.com:zalando-bus/kontrolletti.git"
//    val regex =  s"$protocolRgx$userRgx$hostnameRgx".r
//    val value =  "git@git-hub.com/zalando-bus/kontrolletti.git"
//    val regex(protocol, user, host, prjAntecedent, project, repoAntecedent, repo, succeeder) = value
//    println(succeeder)
//    val values = ParsingData.fixture.protocolUserHostAntecedentsProjectAntecedentRepos
//   
//    for (value <- values) {
//      println(s" VALUE => $value # ")
//      val regex(protocol, user, host, prjAntecedent, project, repoAntecedent, repo, succeeder) = value    
//      println(s"Result $host   --   $project   --   $repo")
//  println(parse("git@git-hub.com:zalando-bus/kontrolletti.git"))
//    }
//}