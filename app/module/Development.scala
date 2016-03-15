package module

import com.google.inject.AbstractModule
import client.{ RequestDispatcher, RequestDispatcherImpl }
import client.kio.{ KioClient, KioClientImpl }
import client.oauth.{ OAuth, OAuthClientImpl }
import configuration.{ GeneralConfiguration, GeneralConfigurationImpl, OAuthConfiguration, OAuthConfigurationImpl }
import dao.{ CommitRepository, CommitRepositoryImpl, RepoRepository, RepoRepositoryImpl }
import play.api.Logger
import service.{ ImportCommit, ImportCommitImpl, ImportRepositoriesImpl, ImportRepository, Search, SearchImpl }
import configuration.SCMConfigurationImpl
import client.scm.StashResolver
import client.scm.SCMResolver
import client.scm.GithubResolver
import configuration.SCMConfiguration
import com.google.inject.name.Names
import client.scm.GithubToJsonParser
import client.scm.SCMParser
import client.scm.StashToJsonParser

class Development extends AbstractModule {

  private val logger: Logger = Logger(this.getClass())
  def configure() {
    logger.info("Configured with the Development module")
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
    ()
  }
}
