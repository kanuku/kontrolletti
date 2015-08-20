package module

import com.google.inject.AbstractModule
import client.RequestDispatcher
import client.RequestDispatcherImpl
import client.kio.KioClient
import client.kio.KioClientConfiguration
import client.kio.KioClientConfigurationImpl
import client.kio.KioClientImpl
import client.oauth.OAuthClient
import client.oauth.OAuthClientImpl
import client.oauth.OAuthConfiguration
import client.oauth.OAuthConfigurationImpl
import client.scm.SCM
import client.scm.SCMImpl
import play.api.Logger
import service.Search
import service.SearchImpl
import service.Synchronizer
import service.SynchronizerImpl
import service.DataStore
import service.DataStoreImpl

class Develop extends AbstractModule {
  private val logger: Logger = Logger(this.getClass())
  def configure() {
    logger.info("Configured with a develop module")
    bind(classOf[Search]).to(classOf[SearchImpl])
    bind(classOf[SCM]).to(classOf[SCMImpl])
    bind(classOf[RequestDispatcher]).to(classOf[RequestDispatcherImpl])
    bind(classOf[Synchronizer]).to(classOf[SynchronizerImpl])
    bind(classOf[OAuthClient]).to(classOf[OAuthClientImpl])
    bind(classOf[OAuthConfiguration]).to(classOf[OAuthConfigurationImpl])
    bind(classOf[KioClient]).to(classOf[KioClientImpl])
    bind(classOf[KioClientConfiguration]).to(classOf[KioClientConfigurationImpl])
    bind(classOf[DataStore]).to(classOf[DataStoreImpl])
 
  }
}