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
      def resourceUri(conf: StashConf, resource: Resource[StashConf]): URI = resource match {
        case Org(id) => URI.create(conf.apiBase.toString + s"/projects/$id")
        case Repo(id, org) => URI.create(resourceUri(conf, org).toString + s"/repos/$id")
        case Commit(id, repo, _) => URI.create(resourceUri(conf, repo).toString + s"/commits/$id")
        case AllCommits(repo) => URI.create(resourceUri(conf, repo).toString + "/commits")
        case AllRepos(org) => URI.create(resourceUri(conf, org).toString + "/repos")
      }
      def nextUri(conf: StashConf, resource: Resource[StashConf], next: Pagination[Resource[StashConf], PaginationRepr]): Option[URI] = ???
    }
  }
}
