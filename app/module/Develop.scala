package module

import com.google.inject.AbstractModule
import client.scm.SCM
import client.scm.SCMImpl
import service.Search
import service.SearchImpl
import play.api.Logger
import client.RequestDispatcherImpl
import client.RequestDispatcher
import client.oauth.OAuthClient
import client.oauth.OAuthClientImpl
import service.Synchronizer
import service.SynchronizerImpl
import client.oauth.OAuthConfigurationImpl
import client.oauth.OAuthConfiguration

class Develop extends AbstractModule {
  private val logger: Logger = Logger(this.getClass())
  def configure() {
    logger.info("Configured with a develop module")
    bind(classOf[Search]).to(classOf[SearchImpl])
    bind(classOf[SCM]).to(classOf[SCMImpl])
    bind(classOf[RequestDispatcher]).to(classOf[RequestDispatcherImpl])
    bind(classOf[OAuthClient]).to(classOf[OAuthClientImpl])
    bind(classOf[Synchronizer]).to(classOf[SynchronizerImpl])
    bind(classOf[OAuthConfiguration]).to(classOf[OAuthConfigurationImpl])
  }
}