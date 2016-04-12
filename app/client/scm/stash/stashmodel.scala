package client.scm.stash

import org.http4s.Uri

import client.scm.Scm
import Scm.Token
import client.scm.scmmodel._

import scalaz.syntax.either._

object stashmodel {

  final case class StashPagination(nextPageStart: Int)
  final case class StashConf(apiBase: Uri, webUiBase: Uri, accessToken: Token, user: String)

  object StashConf {

    implicit val stashScm: Scm[StashConf] = new Scm[StashConf] {
      type PaginationRepr = StashPagination
      def apiBase(conf: StashConf) = conf.apiBase.right
      def webUiBase(conf: StashConf) = conf.webUiBase.right
      def accessToken(conf: StashConf) = conf.accessToken.right
      def user(conf: StashConf) = conf.user.right
      def resourceUri(conf: StashConf, resource: ResourceMeta) = resource match {
        case OrgMeta(id) =>  apiBase(conf).map(_ / "projects" / s"$id")
        case RepoMeta(id, org) => resourceUri(conf, org).map(_ / "repos" / s"$id")
        case CommitMeta(id, repo, _) => resourceUri(conf, repo).map(_ / "commits" / s"$id")
        case AllCommitsMeta(repo) => resourceUri(conf, repo).map(_ / "commits")
        case AllReposMeta(org) => resourceUri(conf, org).map(_ / "repos")
      }
      def paginationUri(conf: StashConf, resource: ResourceMeta, page: Pagination[StashPagination]) = ???
    }
  }
}
