package client.scm

import scmmodel._

import scalaz.{\/, EitherT}
import scalaz.std.option._
import scalaz.syntax.either._
import scalaz.syntax.std.option._
import scalaz.syntax.monad._
import java.net.URI

object github {

  final case class GithubPagination(uri: URI, rel: String)
  final case class GithubConf(apiBase: URI, webUiBase: URI, accessToken: String)

  object GithubConf {
    import Scm.ConfError

    implicit val githubScm: Scm[GithubConf] = new Scm[GithubConf] {
      type PaginationRepr = GithubPagination
      def apiBase(conf: GithubConf) = conf.apiBase.right
      def webUiBase(conf: GithubConf) = conf.webUiBase.right
      def accessToken(conf: GithubConf) = conf.accessToken.right
      def user(conf: GithubConf) = "Github does not require any user".left
      def resourceUri(conf: GithubConf, resource: ResourceMeta[GithubConf]) = resource match {
        case OrgMeta(id) => URI.create(apiBase(conf).toString + s"/repos/$id").right
        case RepoMeta(id, org) => resourceUri(conf, org) map { uri =>
          URI.create(uri.toString + s"/commits/$id")
        }
        case CommitMeta(id, repo, _) => resourceUri(conf, repo) map { uri =>
          URI.create(uri.toString + s"/$id")
        }
        case AllCommitsMeta(repo) => resourceUri(conf, repo) map { uri =>
          URI.create(uri + s"/commits")
        }
        case AllReposMeta(org) => resourceUri(conf, org) map { uri =>
          URI.create(uri + "/repos")
        }
      }
      def nextUri(conf: GithubConf, resource: ResourceMeta[GithubConf], next: Pagination[ResourceMeta[GithubConf], GithubPagination]) = next match {
        case NormalPage(page) =>
          page.uri.some.liftM[EitherT[?[_], ConfError, ?]]
        case _ => None.liftM[EitherT[?[_], ConfError, ?]]
      }
    }
  }
}
