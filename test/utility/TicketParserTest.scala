package utility

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import model.Ticket
import configuration.GeneralConfiguration

/**
 * @author fbenjamin
 */
class TicketParserTest extends FlatSpec with Matchers {
  val jira = "https://jira/browse/"
  val githubEnterprise = "https://github-enterprise.com"
  val github = "https://github.com"
  val project = "zalando"
  val repository = "kontrolletti"
  val text = "class something adapted in a certain way"
  val ticketParser: TicketParser = new TicketParser {
    def githubHost = github
    def githubEnterpriseHost = githubEnterprise
    def jiraTicketUrl = jira
  }

  "TicketParser#parse " must "parse custom [offline:] protocol" in {
    val spec = "offline:BMO/2ndfloor/team_A__space/blue-bookshelf/spec-Whatever"
    val message = s"$spec $text"
    ticketParser.parse(github, project, repository, message) shouldBe Some(new Ticket(message, spec, None))
  }
  it must "parse custom [techjira:] protocol" in {
    val message = "techjira:PF-1234"
    val result = jira + "PF-1234"
    ticketParser.parse(github, project, repository, message) shouldBe Some(new Ticket(message, result, None))
  }
  it must "parse application [https://] protocol" in {
    val spec = "https://github.com/zalando/kontrolletti/issues/55"
    val message = s"$spec $text"
    ticketParser.parse(github, project, repository, message) shouldBe Some(new Ticket(message, spec, None))

  }
  it must "parse application [http://] protocol" in {
    val spec = "http://github.com/zalando/kontrolletti/issues/55"
    val message = s"$spec $text"
    ticketParser.parse(github, project, repository, message) shouldBe Some(new Ticket(message, spec, None))
  }

  /**
   * Tests for issue #Number
   */
  "TicketParser#parse (#Number)" must "parse github issue(#NUMBER) and without host reference" in {
    val message = "#55 adding Ticket model."
    val result = new Ticket(message, s"$github/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(github, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github with an issue(#NUMBER) refering to github itself" in {
    val message = "#55 (gh) adding Ticket model."
    val result = new Ticket(message, s"$github/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(github, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github with an issue(#NUMBER) refering to github-enterprise" in {
    val message = "#55 (ghe) adding Ticket model."
    val result = new Ticket(message, s"$githubEnterprise/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(github, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github-enterprise with an issue(#NUMBER) refering to github" in {
    val message = "#55 (gh) adding Ticket model."
    val result = new Ticket(message, s"$github/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(githubEnterprise, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github-enterprise with an issue(#NUMBER) refering to itself" in {
    val message = "#55 (ghe) adding Ticket model."
    val result = new Ticket(message, s"$githubEnterprise/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(githubEnterprise, project, repository, message) shouldBe Some(result)
  }
  /**
   * Tests for issue GH-Number
   */

  "TicketParser#parse (GH-NUMBER)" must "parse a message on github with an issue(GH-NUMBER) without reference" in {
    val message = "GH-55 adding Ticket model."
    val result = new Ticket(message, s"$github/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(github, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github with an issue(GH-NUMBER) refering to itself" in {
    val message = "GH-55 (gh) adding Ticket model."
    val result = new Ticket(message, s"$github/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(github, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github with an issue(GH-NUMBER) refering to github-enterprise" in {
    val message = "GH-55 (ghe) adding Ticket model."
    val result = new Ticket(message, s"$githubEnterprise/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(github, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github-enterprise with an issue(GH-NUMBER) without reference" in {
    val message = "GH-55 adding Ticket model."
    val result = new Ticket(message, s"$githubEnterprise/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(githubEnterprise, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github-enterprise with an issue(GH-NUMBER) refering to github" in {
    val message = "GH-55 (gh) adding Ticket model."
    val result = new Ticket(message, s"$github/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(githubEnterprise, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github-enterprise with an issue(GH-NUMBER) refering to itself" in {
    val message = "GH-55 (ghe) adding Ticket model."
    val result = new Ticket(message, s"$githubEnterprise/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(githubEnterprise, project, repository, message) shouldBe Some(result)
  }

  /**
   * Tests for issuehost/project/repository#Number Is not a must...
   */
  "TicketParser#parse host/project/repository#Number" must "parse a message on github with an issue(zalando/kontrolletti#55) without reference" in {
    val message = "zalando/kontrolletti#55 adding Ticket model."
    val result = new Ticket(message, s"$github/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(github, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github with an issue(zalando/kontrolletti#55) refering to itself" in {
    val message = "zalando/kontrolletti#55 (gh) adding Ticket model."
    val result = new Ticket(message, s"$github/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(github, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github with an issue(zalando/kontrolletti#55) refering to github-enterprise" in {
    val message = "zalando/kontrolletti#55 (ghe) adding Ticket model."
    val result = new Ticket(message, s"$githubEnterprise/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(github, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github-enterprise with an issue(zalando/kontrolletti#55) without reference" in {
    val message = "zalando/kontrolletti#55 adding Ticket model."
    val result = new Ticket(message, s"$githubEnterprise/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(githubEnterprise, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github-enterprise with an issue(zalando/kontrolletti#55) refering itself" in {
    val message = "zalando/kontrolletti#55 (ghe) adding Ticket model."
    val result = new Ticket(message, s"$githubEnterprise/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(githubEnterprise, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github-enterprise with an issue(zalando/kontrolletti#55) refering to github" in {
    val message = "zalando/kontrolletti#55 (gh) adding Ticket model."
    val result = new Ticket(message, s"$github/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(githubEnterprise, project, repository, message) shouldBe Some(result)
  }

  /**
   * For very specific cases
   */

  "TicketParser#parse (#Number)" must "parse github issue(#NUMBER) in the middel of the msg" in {
    val message = "Finished #55. Adding Ticket model."
    val result = new Ticket(message, s"$github/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(github, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github with an issue(#NUMBER) in the middel of the msg, without space, refering to github itself" in {
    val message = "Finished#55(gh)adding Ticket model."
    val result = new Ticket(message, s"$github/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(github, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github-enterprise with an issue(GH-NUMBER) refering to itself" in {
    val message = "Great,GH-55(ghe)is done, by this commit."
    val result = new Ticket(message, s"$githubEnterprise/zalando/kontrolletti/issues/55", None)
    ticketParser.parse(githubEnterprise, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github-enterprise with an issue(GH-NUMBER) at the end, refering to itself" in {
    val message = "Parse valid message into a ticket. fixes #98"
    val result = new Ticket(message, s"$githubEnterprise/zalando/kontrolletti/issues/98", None)
    ticketParser.parse(githubEnterprise, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message on github-enterprise with an issue, refering to itself" in {
    val message = "Merge pull request #63 from zalando/get-credentials-from-s3"
    val result = new Ticket(message, s"$githubEnterprise/zalando/kontrolletti/issues/63", None)
    ticketParser.parse(githubEnterprise, project, repository, message) shouldBe Some(result)
  }
  it must "parse a message from kontrolletti" in {
    val message = "Merge pull request #63 from zalando/get-credentials-from-s3"
    val result = new Ticket(message, s"https://github.com/zalando/kontrolletti/issues/63", None)
    ticketParser.parse("github.com", "zalando", "kontrolletti", message) shouldBe Some(result)
  }
  ignore must "parse a message from kontrolletti with multiple tickets" in {
    val message = "Merge pull request #104 from zalando/ticket-parsing\n\nParse valid message into a ticket. fixes #98"
    val result = new Ticket(message, s"https://github.com/zalando/kontrolletti/issues/63", None)
    ticketParser.parse("github.com", "zalando", "kontrolletti", message) shouldBe Some(result)
  }
}
