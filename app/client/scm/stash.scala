package client.scm

import scmmodel._

import scalaz.syntax.either._

import java.net.URI

object stash {

  final case class StashPagination(nextPageStart: Int)
  final case class StashConf(apiBase: URI, webUiBase: URI, accessToken: String, user: String)

  object StashConf {

    implicit val stashScm: Scm[StashConf] = new Scm[StashConf] {
      type PaginationRepr = StashPagination
      def apiBase(conf: StashConf) = conf.apiBase.right
      def webUiBase(conf: StashConf) = conf.webUiBase.right
      def accessToken(conf: StashConf) = conf.accessToken.right
      def user(conf: StashConf) = conf.user.right
      def resourceUri(conf: StashConf, resource: ResourceMeta[StashConf]): URI = resource match {
        case OrgMeta(id) => URI.create(conf.apiBase.toString + s"/projects/$id")
        case RepoMeta(id, org) => URI.create(resourceUri(conf, org).toString + s"/repos/$id")
        case CommitMeta(id, repo, _) => URI.create(resourceUri(conf, repo).toString + s"/commits/$id")
        case AllCommitsMeta(repo) => URI.create(resourceUri(conf, repo).toString + "/commits")
        case AllReposMeta(org) => URI.create(resourceUri(conf, org).toString + "/repos")
      }
      def nextUri(conf: StashConf, resource: ResourceMeta[StashConf], next: Pagination[ResourceMeta[StashConf], PaginationRepr]): Option[URI] = ???
    }
  }
}
