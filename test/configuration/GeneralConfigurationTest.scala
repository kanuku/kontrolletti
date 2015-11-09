package configuration

import org.scalatestplus.play.{ OneAppPerSuite, PlaySpec }
import test.util.ConfigurableFakeApp
import org.scalatest.Matchers._

class GeneralConfigurationTest extends PlaySpec with ConfigurableFakeApp with OneAppPerSuite {
  val conf: GeneralConfiguration = new GeneralConfigurationImpl

  implicit override lazy val app = fakeApplication

  override def configuration: Map[String, _] = Map(
    "client.kio.service.apps.endpoint" -> "https://kio.io/endpoint",
    "default.client.timeout" -> "20000",
    "ticket.reference.github.host" -> "https://github.com/",
    "ticket.reference.github-enterprise.host" -> "https://github-enterprise.com/",
    "ticket.reference.jira.tickets.url" -> "https://jira.com/browse/")

  "GeneralConfiguration#kioServiceAppsEndpoint" should {
    "return endpoint for kio server" in {
      conf.kioServiceAppsEndpoint shouldBe "https://kio.io/endpoint"
    }
  }
  "GeneralConfiguration#defaultClientTimeout" should {
    "return the default client timeout" in {
      conf.defaultClientTimeout shouldBe 20000
    }
  }
  "GeneralConfiguration#ticketReferenceGithubHost" should {
    "return the default https URL for the Githubhost" in {
      conf.ticketReferenceGithubHost shouldBe "https://github.com/"
    }
  }
  "GeneralConfiguration#ticketReferenceGithubEnterpriseHost" should {
    "return the default https URL for the Githubhost" in {
      conf.ticketReferenceGithubEnterpriseHost shouldBe "https://github-enterprise.com/"
    }
  }
  "GeneralConfiguration#ticketReferenceJiraBrowseUrl" should {
    "return the default JIRA URL for browsing tickets" in {
      conf.ticketReferenceJiraBrowseUrl shouldBe "https://jira.com/browse/"
    }
  }

}
