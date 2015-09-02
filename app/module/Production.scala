package module

import com.google.inject.AbstractModule
import client.RequestDispatcher
import client.RequestDispatcherImpl
import client.kio.KioClient
import client.kio.KioClientConfiguration
import client.kio.KioClientImpl
import client.oauth.OAuth
import client.oauth.OAuthClientImpl
import client.oauth.OAuthConfiguration
import client.oauth.OAuthConfigurationImpl
import client.scm.SCM
import client.scm.SCMImpl
import play.api.Logger
import service.Search
import service.SearchImpl
import jobs.Import
import jobs.ImportImpl
import client.kio.KioClientConfigurationImpl
import service.DataStore
import service.DataStoreImpl

class Production extends AbstractModule {
  private val logger: Logger = Logger(this.getClass())
  def configure() {
    logger.info("Configured with a production module")
    bind(classOf[Import]).to(classOf[ImportImpl]).asEagerSingleton()
  }
}