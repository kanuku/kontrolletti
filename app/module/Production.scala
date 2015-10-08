package module

import com.google.inject.AbstractModule

import client.RequestDispatcher
import client.RequestDispatcherImpl
import client.kio.KioClient
import client.kio.KioClientImpl
import client.oauth.OAuth
import client.oauth.OAuthClientImpl
import configuration.GeneralConfiguration
import configuration.GeneralConfigurationImpl
import configuration.OAuthConfiguration
import configuration.OAuthConfigurationImpl
import dao.AuthorRepository
import dao.AuthorRepositoryImpl
import dao.CommitRepository
import dao.CommitRepositoryImpl
import dao.RepoRepository
import dao.RepoRepositoryImpl
import play.api.Logger
import service.Import
import service.ImportImpl
import service.Search
import service.SearchImpl

class Production extends AbstractModule {

  private val logger: Logger = Logger(this.getClass())
  def configure() {
    logger.info("Configured with a production module")
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
    bind(classOf[Bootstrap]).to(classOf[BootstrapImpl]).asEagerSingleton()
  }
}