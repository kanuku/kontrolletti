package module

import com.google.inject.AbstractModule
import client.kio.KioClient
import configuration.GeneralConfiguration
import configuration.GeneralConfigurationImpl
import client.kio.KioClientImpl
import client.oauth.OAuth
import client.oauth.OAuthClientImpl
import service.Import
import service.ImportImpl
import play.api.Logger
import service.Search
import service.SearchImpl
import client.RequestDispatcherImpl
import client.RequestDispatcher
import dao.CommitRepositoryImpl
import dao.CommitRepository
import dao.AuthorRepository
import dao.AuthorRepositoryImpl
import dao.RepoRepository
import dao.RepoRepositoryImpl
import configuration.OAuthConfigurationImpl
import configuration.OAuthConfiguration

class Development extends AbstractModule {

  private val logger: Logger = Logger(this.getClass())
  def configure() {
    logger.info("Configured with the Development module")
    bind(classOf[OAuth]).to(classOf[OAuthClientImpl])
    bind(classOf[GeneralConfiguration]).to(classOf[GeneralConfigurationImpl])
    bind(classOf[OAuthConfiguration]).to(classOf[OAuthConfigurationImpl])
    bind(classOf[RequestDispatcher]).to(classOf[RequestDispatcherImpl])
    bind(classOf[GeneralConfiguration]).to(classOf[GeneralConfigurationImpl])
    bind(classOf[KioClient]).to(classOf[KioClientImpl])
    bind(classOf[Search]).to(classOf[SearchImpl])
    bind(classOf[Import]).to(classOf[ImportImpl])
    bind(classOf[CommitRepository]).to(classOf[CommitRepositoryImpl])
    bind(classOf[RepoRepository]).to(classOf[RepoRepositoryImpl])
    bind(classOf[AuthorRepository]).to(classOf[AuthorRepositoryImpl])
  }
}