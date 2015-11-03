package module

import com.google.inject.AbstractModule

import client.{ RequestDispatcher, RequestDispatcherImpl }
import client.kio.{ KioClient, KioClientImpl }
import client.oauth.{ OAuth, OAuthClientImpl }
import configuration.{ GeneralConfiguration, GeneralConfigurationImpl, OAuthConfiguration, OAuthConfigurationImpl }
import dao.{ CommitRepository, CommitRepositoryImpl, RepoRepository, RepoRepositoryImpl }
import play.api.Logger
import service.{ ImportCommit, ImportCommitImpl, ImportRepositoriesImpl, ImportRepository, Search, SearchImpl }

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
    bind(classOf[ImportRepository]).to(classOf[ImportRepositoriesImpl])
    bind(classOf[ImportCommit]).to(classOf[ImportCommitImpl])
    bind(classOf[CommitRepository]).to(classOf[CommitRepositoryImpl])
    bind(classOf[RepoRepository]).to(classOf[RepoRepositoryImpl])
  }
}