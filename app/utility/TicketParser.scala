package utility

import model.Ticket
import configuration.GeneralConfiguration
import play.api.Logger

/**
 * @author fbenjamin
 */
trait TicketParser {
  private val logger: Logger = Logger(this.getClass())

  /**
   * The following are the small pieces that need to be extracted from the message.
   *  Reference:https://help.github.com/articles/writing-on-github/#references
   *
   */
  private val message = """(.*?)?"""
  private val offline = """(.*?offline:[\w*\.*/*\-*]+){1}"""
  private val techJira = """(.*?techjira:|jira:){1}"""
  private val jiraSpec = """(.*?[\w*\-*\\d*]+){1}"""
  private val https = """(.*?https://[\w*\.*/*\-*]+){1}"""
  private val http = """(.*?http://[\w*\.*/*\-*]+){1}"""
  private val number = """(\d+){1}"""
  private val issueHashtag = """(.*?#){1}"""
  private val referenceGithub = """(\s*?\(gh\)\s*?){1}"""
  private val referenceGithubEnterprise = """(\s*?\(ghe\)\s*?){1}"""
  private val issueGH = """(.*?GH-){1}"""
  private val project = """([\w-.]+){1,1}"""
  private val repoRgx = """([\w.-]*){1,1}"""
  /**
   * Here we compose the use-cases we want to extract.
   */
  private val offlineRegex = s"$offline$message".r
  private val jiraRegex = s"$techJira$jiraSpec$message".r
  private val httpsRegex = s"$https$message".r
  private val httpRegex = s"$http$message".r
  //Reference like #33 will refer to the current host/project/repository i.o.w. to itself(github/github-enterprise)
  private val issueOnItselfRegex = s"$issueHashtag$number$message".r
  //Reference like #33 (gh) will refer to the github host configured: [github.com]/project/repository/issues/number
  private val issueOnGithubRegex = s"$issueHashtag$number$referenceGithub$message".r
  //Reference like #33 (ghe) will refer to the github-enterprise host configured: [github.com]/project/repository
  private val issueOnGithubEnterpriseRegex = s"$issueHashtag$number$referenceGithubEnterprise$message".r
  //Reference like GH-33  will refer to the current host/project/repository i.o.w. to itself(github/github-enterprise)
  private val issueGHOnItselfRegex = s"$issueGH$number$message".r
  //Reference like GH-33 (gh) will refer to the github host configured: [github.com]/project/repository
  private val issueGHOnGithubRegex = s"$issueGH$number$referenceGithub$message".r
  //Reference like GH-33 (ghe) will refer to the github-enterprise host configured: [github.com]/project/repository
  private val issueGHOnGithubEnterpriseRegex = s"$issueGH$number$referenceGithubEnterprise$message".r

  def parse(host: String, project: String, repository: String, message: String): Option[Ticket] = message match {
    // CAUTION: The order of the regex must be kept as is. It is ordered in regex-priority.
    case offlineRegex(specLink, restCap) =>
      Some(Ticket(message, specLink, None))
    case jiraRegex(jiraCap, specCap, restCap) =>
      val link = jiraTicketUrl + specCap
      Some(Ticket(message, link, None))
    case httpsRegex(specLink, restCap) =>
      Some(Ticket(message, specLink, None))
    case httpRegex(specLink, restCap) =>
      Some(Ticket(message, specLink, None))
    case issueOnGithubRegex(specLink, numberCap, reference, restCap) =>
      Some(Ticket(message, s"$githubHost/$project/$repository/issues/$numberCap", None))
    case issueOnGithubEnterpriseRegex(specLink, numberCap, reference, restCap) =>
      Some(Ticket(message, s"$githubEnterpriseHost/$project/$repository/issues/$numberCap", None))
    case issueGHOnGithubRegex(specLink, numberCap, reference, restCap) =>
      Some(Ticket(message, s"$githubHost/$project/$repository/issues/$numberCap", None))
    case issueGHOnGithubEnterpriseRegex(specLink, numberCap, reference, restCap) =>
      Some(Ticket(message, s"$githubEnterpriseHost/$project/$repository/issues/$numberCap", None))
    case issueGHOnItselfRegex(specLink, numberCap, restCap) =>
      if (host.toLowerCase().startsWith("http"))
        Some(Ticket(message, s"$host/$project/$repository/issues/$numberCap", None))
      else
        Some(Ticket(message, s"https://$host/$project/$repository/issues/$numberCap", None))
    case issueOnItselfRegex(specLink, numberCap, restCap) =>
      if (host.toLowerCase().startsWith("http"))
        Some(Ticket(message, s"$host/$project/$repository/issues/$numberCap", None))
      else
        Some(Ticket(message, s"https://$host/$project/$repository/issues/$numberCap", None))
    case _ =>
      logger.info(s"Failed to parse ->  message:$message")
      None
  }
  def jiraTicketUrl: String
  def githubHost: String
  def githubEnterpriseHost: String
}

//object Ay extends App with TicketParser {
//  private val issueHashtag = """(.*?#){1}"""
//  private val number = """(\d+){1}"""
//  private val message = """(.*?)?"""
//
//  val jira = "https://jira/browse/"
//  val githubEnterprise = "https://github-enterprise.com"
//  val github = "https://github.com"
//
//  def githubHost = github
//  def githubEnterpriseHost = githubEnterprise
//  def jiraTicketUrl = jira
//
//  private val issueOnItselfRegex = s"$issueHashtag$number$message".r
//  def d(value: String) = value match {
//    case issueOnItselfRegex(issue, number, message) => println(s"Value: $number")
//    case _ => println(value)
//
//  }
//
//  d("Merge pull request #104 from zalando/ticket-parsing\n\nParse valid message into a ticket. fixes #98")
//}
