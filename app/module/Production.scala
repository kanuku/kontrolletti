package module

import com.google.inject.AbstractModule
import client.kio.KioClient
import client.kio.KioClientConfiguration
import client.kio.KioClientConfigurationImpl
import client.kio.KioClientImpl
import client.oauth.OAuth
import client.oauth.OAuthClientImpl
import service.Import
import service.ImportImpl
import play.api.Logger
import service.DataStore
import service.DataStoreImpl
import service.Search
import service.SearchImpl
import dao.AppInfoRepository
import dao.AppInfoRepositoryImpl
import client.RequestDispatcherImpl
import client.RequestDispatcher
import dao.CommitRepositoryImpl
import dao.CommitRepository

class Production extends AbstractModule {

  private val logger: Logger = Logger(this.getClass())
  def configure() {
    logger.info("Configured with a production module")
    bind(classOf[OAuth]).to(classOf[OAuthClientImpl])
    bind(classOf[RequestDispatcher]).to(classOf[RequestDispatcherImpl])
    bind(classOf[DataStore]).to(classOf[DataStoreImpl])
    bind(classOf[KioClientConfiguration]).to(classOf[KioClientConfigurationImpl])
    bind(classOf[KioClient]).to(classOf[KioClientImpl])
    bind(classOf[Search]).to(classOf[SearchImpl])
    bind(classOf[Import]).to(classOf[ImportImpl])
    bind(classOf[AppInfoRepository]).to(classOf[AppInfoRepositoryImpl])
    bind(classOf[CommitRepository]).to(classOf[CommitRepositoryImpl])
    bind(classOf[Bootstrap]).to(classOf[BootstrapImpl]).asEagerSingleton()
  }
}