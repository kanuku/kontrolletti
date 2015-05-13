package v1.util

import play.api.Logger

/**
 * An utility trait
 *
 */
trait UrlParser {

  val projectRgx = """(\w*[-.\w]*)"""
  val projectAntecedentRgx = """(/projects/|/)+"""
  val hostnameRgx = """(\w+[-.\w*]*\w*[:\d]*)+"""
  val userRgx = """(\w+@)*"""
  val protocolRgx = """(\w+://)*"""
  val regex01 = s"$protocolRgx$userRgx$hostnameRgx/$projectAntecedentRgx/$projectRgx".r
  val regex02 = s"$projectRgx".r
  val regex03 = s"$protocolRgx$userRgx$hostnameRgx".r
  val regex04 = s"$protocolRgx$userRgx$hostnameRgx$projectAntecedentRgx".r
  val regex05 = s"$protocolRgx$userRgx$hostnameRgx$projectAntecedentRgx$projectRgx".r

  /**
   * Splits the repository-url of a github project into three parts:
   *  Host , project(user/org) , repository
   *
   *  @param url the url to be split
   *  @return the `host`, the `project` and the `repo`
   */
  def parse(url: String): (String, String, String) = {
//    Logger.error(s" #### $url ")
    url match {
      //      case regex02(project) =>
      //        Logger.error(s"regex02 $url => protocol(protocol) user(user) host(host) project($project)")
      //        (null, project, null)
      //      case regex01(protocol, user, host, antecedent, project) =>
      //        Logger.error(s"regex01 $url ")
      //        Logger.error(s"$protocol # $user # $host # $antecedent # $project")
      //        (host, null, null)
      case regex03(protocol, user, host) =>
        Logger.error(s"regex01 $url => protocol($protocol) user($user) host(host) ")
        (host, null, null)
//      case regex04(protocol, user, host,antecedent) =>
//        Logger.error(s"regex01 $url => protocol($protocol) user($user) host(host) antecedent($antecedent)")
//        (host, null, null)
//      case regex05(protocol, user, host,antecedent, project) =>
//        Logger.error(s"regex01 $url => protocol($protocol) user($user) host(host) antecedent($antecedent) project($project)")
//        (host, null, null)
      case _ =>
        Logger.error(s"Could not parse $url")
        ("", "", "")
    }
  }
}

//object A extends App with UrlParser {
//	import v1.test.util.ParsingData
//  val regex = s"$hostnameRgx".r
//  val values = ParsingData.fixture.hosts
//  for (value <- values){
//    val regex(result) = value
//    println(s"$value => $result")
//    //    println(parse(value))
//  }
//
//}