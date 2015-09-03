package module

import com.google.inject.AbstractModule
import com.google.inject.ImplementedBy
import client.oauth.OAuth
import client.oauth.OAuthClientImpl
import jobs.Import
import jobs.ImportImpl
import play.api.Logger
import service.DataStore
import service.DataStoreImpl
import client.kio.KioClient
import client.kio.KioClientImpl
import client.kio.KioClientConfiguration
import client.kio.KioClientConfigurationImpl
import service.Search
import service.SearchImpl
import dao.DataStoreDAO
import dao.DataStoreDAOImpl
import play.api.libs.concurrent.AkkaGuiceSupport

class Production extends AbstractModule with AkkaGuiceSupport {
  

  private val logger: Logger = Logger(this.getClass())
  def configure() {
    logger.info("Configured with a production module")
    bind(classOf[OAuth]).to(classOf[OAuthClientImpl])
    bind(classOf[DataStore]).to(classOf[DataStoreImpl])
    bind(classOf[KioClient]).to(classOf[KioClientImpl])
    bind(classOf[KioClientConfiguration]).to(classOf[KioClientConfigurationImpl])
    bind(classOf[Search]).to(classOf[SearchImpl])
    bind(classOf[DataStoreDAO]).to(classOf[DataStoreDAOImpl])
    bind(classOf[Import]).to(classOf[ImportImpl]).asEagerSingleton()
//    bindActor[ConfiguredActor]("configured-actor")
  }
}