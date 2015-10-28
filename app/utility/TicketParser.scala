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
  val message = """(.*)?"""
  val offline = """(.?offline:[\w*\.*/*\-*]+){1}"""
  val techJira = """(.?techjira:|jira:){1}"""
  val jiraSpec = """(.?[\w*\-*\\d*]+){1}"""
  val https = """(.?https://[\w*\.*/*\-*]+){1}"""
  val http = """(.?http://[\w*\.*/*\-*]+){1}"""
  val number = """(\d+){1}"""
  val issueHashtag = """(.?#){1}"""
  val referenceGithub = """(\s*\(gh\)\s*){1}"""
  val referenceGithubEnterprise = """(\s*\(ghe\)\s*){1}"""
  val issueGH = """(.?GH-){1}"""
  val project = """([\w-.]+){1,1}"""
  val repoRgx = """([\w.-]*?){1,1}"""
  /**
   * Here we compose the use-cases we want to extract.
   */
  val offlineRegex = s"$offline$message".r
  val jiraRegex = s"$techJira$jiraSpec$message".r
  val httpsRegex = s"$https$message".r
  val httpRegex = s"$http$message".r
  //Reference like #33 will refer to the current host/project/repository i.o.w. to itself(github/github-enterprise)
  val issueOnItselfRegex = s"$issueHashtag$number$message".r
  //Reference like #33 (gh) will refer to the github host configured: [github.com]/project/repository/issues/number
  val issueOnGithubRegex = s"$issueHashtag$number$referenceGithub$message".r
  //Reference like #33 (ghe) will refer to the github-enterprise host configured: [github.com]/project/repository
  val issueOnGithubEnterpriseRegex = s"$issueHashtag$number$referenceGithubEnterprise$message".r
  //Reference like GH-33  will refer to the current host/project/repository i.o.w. to itself(github/github-enterprise)
  val issueGHOnItselfRegex = s"$issueGH$number$message".r
  //Reference like GH-33 (gh) will refer to the github host configured: [github.com]/project/repository
  val issueGHOnGithubRegex = s"$issueGH$number$referenceGithub$message".r
  //Reference like GH-33 (ghe) will refer to the github-enterprise host configured: [github.com]/project/repository
  val issueGHOnGithubEnterpriseRegex = s"$issueGH$number$referenceGithubEnterprise$message".r

  def parse(host: String, project: String, repository: String, message: String): Option[Ticket] = message match {
    // CAUTION: The order of the regex must be kept as is. It is ordered in regex-priority.
    case offlineRegex(specLink, restCap) =>
      logger.info(s"Parser -> [offline:] host:$host, project:$project, repository:$repository, spec:$specLink rest:$restCap")
      Some(Ticket(message, specLink, None))
    case jiraRegex(jiraCap, specCap, restCap) =>
      logger.info(s"Parser -> [jira:] host:$host, project:$project, repository:$repository, spec:$specCap rest:$restCap")
      val link = jiraHost + specCap
      Some(Ticket(message, link, None))
    case httpsRegex(specLink, restCap) =>
      logger.info(s"Parser -> [https://] host:$host, project:$project, repository:$repository, spec:$specLink rest:$restCap")
      Some(Ticket(message, specLink, None))
    case httpRegex(specLink, restCap) =>
      logger.info(s"Parser -> [http://] host:$host, project:$project, repository:$repository, spec:$specLink rest:$restCap")
      Some(Ticket(message, specLink, None))
    case issueOnGithubRegex(specLink, numberCap, reference, restCap) =>
      logger.info(s"Parser -> [#Number (gh)] host:$host, project:$project, repository:$repository, number: $numberCap rest:$restCap")
      Some(Ticket(message, s"$githubHost/$project/$repository/issues/$numberCap", None))
    case issueOnGithubEnterpriseRegex(specLink, numberCap, reference, restCap) =>
      logger.info(s"Parser -> [#Number (ghe)] host:$host, project:$project, repository:$repository, number: $numberCap rest:$restCap")
      Some(Ticket(message, s"$githubEnterpriseHost/$project/$repository/issues/$numberCap", None))
    case issueGHOnGithubRegex(specLink, numberCap, reference, restCap) =>
      logger.info(s"Parser -> [GH-Number (gh)] host:$host, project:$project, repository:$repository, number: $numberCap rest:$restCap")
      Some(Ticket(message, s"$githubHost/$project/$repository/issues/$numberCap", None))
    case issueGHOnGithubEnterpriseRegex(specLink, numberCap, reference, restCap) =>
      logger.info(s"Parser -> [GH-Number (ghe)] host:$host, project:$project, repository:$repository, number: $numberCap rest:$restCap")
      Some(Ticket(message, s"$githubEnterpriseHost/$project/$repository/issues/$numberCap", None))
    case issueGHOnItselfRegex(specLink, numberCap, restCap) =>
      logger.info(s"Parser -> [GH-Number (gh)] host:$host, project:$project, repository:$repository, number: $numberCap rest:$restCap")
      Some(Ticket(message, s"$host/$project/$repository/issues/$numberCap", None))
    case issueOnItselfRegex(specLink, numberCap, restCap) =>
      logger.info(s"Parser -> [#Number] host:$host, project:$project, repository:$repository, number: $numberCap rest:$restCap")
      Some(Ticket(message, s"$host/$project/$repository/issues/$numberCap", None))
    case _ =>
      logger.info(s"Failed to parse -> none: host:$host, project:$project, repository:$repository, message:$message")
      None
  }

  def jiraHost: String
  def githubHost: String
  def githubEnterpriseHost: String
}
