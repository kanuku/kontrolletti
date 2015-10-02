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

class Development extends AbstractModule {

  private val logger: Logger = Logger(this.getClass())
  def configure() {
    logger.info("Configured with the Development module")
    bind(classOf[OAuth]).to(classOf[OAuthClientImpl])
    bind(classOf[RequestDispatcher]).to(classOf[RequestDispatcherImpl])
    bind(classOf[KioClientConfiguration]).to(classOf[KioClientConfigurationImpl])
    bind(classOf[KioClient]).to(classOf[KioClientImpl])
    bind(classOf[Search]).to(classOf[SearchImpl])
    bind(classOf[Import]).to(classOf[ImportImpl])
    bind(classOf[RepoRepository]).to(classOf[RepoRepositoryImpl])
    bind(classOf[CommitRepository]).to(classOf[CommitRepositoryImpl])
    bind(classOf[AuthorRepository]).to(classOf[AuthorRepositoryImpl])
  }
}