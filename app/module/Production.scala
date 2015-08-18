package module

import com.google.inject.AbstractModule
import service.SearchImpl
import service.Search
import client.scm.SCM
import client.scm.SCMImpl
import play.api.Logger
import client.RequestDispatcherImpl
import client.RequestDispatcher
import client.oauth.OAuthClientImpl
import client.oauth.OAuthClient
import service.SynchronizerImpl
import service.Synchronizer
import client.oauth.OAuthConfigurationImpl
import client.oauth.OAuthConfiguration

class Production extends AbstractModule {
  private val logger: Logger = Logger(this.getClass())
  def configure() {
    logger.info("Configured with a production module")
    bind(classOf[Search]).to(classOf[SearchImpl])
    bind(classOf[SCM]).to(classOf[SCMImpl])
    bind(classOf[RequestDispatcher]).to(classOf[RequestDispatcherImpl])
    bind(classOf[OAuthClient]).to(classOf[OAuthClientImpl])
    bind(classOf[Synchronizer]).to(classOf[SynchronizerImpl])
    bind(classOf[OAuthConfiguration]).to(classOf[OAuthConfigurationImpl])
  }
}