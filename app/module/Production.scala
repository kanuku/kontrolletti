package module

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import client.{ RequestDispatcher, RequestDispatcherImpl }
import client.kio.{ KioClient, KioClientImpl }
import client.oauth.{ OAuth, OAuthClientImpl }
import client.scmdeprecate.{ GithubResolver, SCMResolver, StashResolver }
import configuration.{ GeneralConfiguration, GeneralConfigurationImpl, OAuthConfiguration, OAuthConfigurationImpl }
import dao.{ CommitRepository, CommitRepositoryImpl, RepoRepository, RepoRepositoryImpl }
import play.api.Logger
import service.{ ImportCommit, ImportCommitImpl, ImportRepositoriesImpl, ImportRepository, Search, SearchImpl }
import configuration.SCMConfigurationImpl
import configuration.SCMConfiguration
import client.scmdeprecate.SCMParser
import client.scmdeprecate.GithubToJsonParser
import client.scmdeprecate.StashToJsonParser

class Production extends AbstractModule {

  private val logger: Logger = Logger(this.getClass())
  def configure() {
    logger.info("Configured with a production module")
    bind(classOf[OAuth]).to(classOf[OAuthClientImpl])
    bind(classOf[GeneralConfiguration]).to(classOf[GeneralConfigurationImpl])
    bind(classOf[SCMConfiguration]).to(classOf[SCMConfigurationImpl])
    bind(classOf[OAuthConfiguration]).to(classOf[OAuthConfigurationImpl])
    bind(classOf[RequestDispatcher]).to(classOf[RequestDispatcherImpl])
    bind(classOf[KioClient]).to(classOf[KioClientImpl])
    bind(classOf[Search]).to(classOf[SearchImpl])
    bind(classOf[ImportRepository]).to(classOf[ImportRepositoriesImpl])
    bind(classOf[ImportCommit]).to(classOf[ImportCommitImpl])
    bind(classOf[CommitRepository]).to(classOf[CommitRepositoryImpl])
    bind(classOf[RepoRepository]).to(classOf[RepoRepositoryImpl])
    bind(classOf[SCMResolver]).annotatedWith(Names.named("github")).to(classOf[GithubResolver])
    bind(classOf[SCMResolver]).annotatedWith(Names.named("stash")).to(classOf[StashResolver])
    bind(classOf[SCMParser]).annotatedWith(Names.named("github")).to(classOf[GithubToJsonParser])
    bind(classOf[SCMParser]).annotatedWith(Names.named("stash")).to(classOf[StashToJsonParser])
    bind(classOf[Bootstrap]).to(classOf[BootstrapImpl]).asEagerSingleton()
  }
}