package configuration

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.OneAppPerSuite
import test.util.ConfigurableFakeApp

class OAuthConfigurationTest extends PlaySpec with ConfigurableFakeApp with OneAppPerSuite {
  val oauthConfiguration: OAuthConfiguration = new OAuthConfigurationImpl

  val excludedPaths = Set("/swagger", "/assets", "/status", "/specs")
  implicit override lazy val app = fakeApplication

  override def configuration: Map[String, _] = Map(
    "play.http.filters" -> null, //We need to disable filtering :D
    "client.oauth.credentials.dir" -> "test.client.oauth.credentials.dir",
    "client.oauth.access.token.service.url" -> "test.client.oauth.access.token.service.url",
    "client.oauth.token.info.service.url" -> "test.client.oauth.token.info.service.url",
    "client.oauth.request.timeout.ms" -> "4500",
    "client.oauth.realm" -> "test.client.oauth.realm",
    "client.oauth.client.credentials.file" -> "test.client.oauth.client.credentials.file",
    "client.oauth.user.credentials.file" -> "test.client.oauth.user.credentials.file",
    "service.oauth.exclude.paths" -> excludedPaths)

  "OAuthConfiguration" should {

    "Load accessTokenRequestEndpoint" in {
      assert(oauthConfiguration.accessTokenRequestEndpoint == "test.client.oauth.access.token.service.url")
    }
    "Load clientCredentialsFilename" in {
      assert(oauthConfiguration.clientCredentialsFilename == "test.client.oauth.client.credentials.file")
    }
    "Load credentialsDirectory" in {
      assert(oauthConfiguration.credentialsDirectory == "test.client.oauth.credentials.dir")
    }
    "Load requestClientTimeout" in {
      assert(oauthConfiguration.requestClientTimeout == 4500)
    }
    "Load tokenInfoRequestEndpoint" in {
      assert(oauthConfiguration.tokenInfoRequestEndpoint == "test.client.oauth.token.info.service.url")
    }
    "Load userCredentialsFileName" in {
      assert(oauthConfiguration.userCredentialsFileName == "test.client.oauth.user.credentials.file")
    }
    "Load excludedPaths" in {
      val result = oauthConfiguration.excludedPaths
      assert(result.size == excludedPaths.size, "Excluded path")
      assert(result.exists(excludedPaths.contains(_)), "Excluded path")
    }

  }

}
