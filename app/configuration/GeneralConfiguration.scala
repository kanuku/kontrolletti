package configuration
import javax.inject.Singleton
import play.api.Play
trait GeneralConfiguration {
  def kioServiceAppsEndpoint: String
  def defaultClientTimeout: Int
}

@Singleton
class GeneralConfigurationImpl extends GeneralConfiguration {
  def defaultClientTimeout: Int = Play.current.configuration.getInt("default.client.timeout").get
  def kioServiceAppsEndpoint: String = Play.current.configuration.getString("client.kio.service.apps.endpoint").get
}