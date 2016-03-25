package client.scm

import scmmodel._

import scalaz.syntax.either._
import java.net.URI

object github {

  final case class GithubPagination(uri: URI, rel: String)
  final case class GithubConf(apiBase: URI, webUiBase: URI, accessToken: String)

  object GithubConf {

    implicit val githubScm: Scm[GithubConf] = new Scm[GithubConf] {
      type PaginationRepr = GithubPagination
      def apiBase(conf: GithubConf) = conf.apiBase.right
      def webUiBase(conf: GithubConf) = conf.webUiBase.right
      def accessToken(conf: GithubConf) = conf.accessToken.right
      def user(conf: GithubConf) = "Github does not require any user".left
      def resourceUri(conf: GithubConf, resource: ResourceMeta[GithubConf]): URI = resource match {
        case OrgMeta(id) => URI.create(apiBase(conf).toString + s"/repos/$id")
        case RepoMeta(id, org) => URI.create(resourceUri(conf, org).toString + s"/commits/$id")
        case CommitMeta(id, repo, _) => URI.create(resourceUri(conf, repo).toString + s"/$id")
        case AllCommitsMeta(repo) => URI.create(resourceUri(conf, repo).toString + s"/commits")
        case AllReposMeta(org) => URI.create(resourceUri(conf, org).toString + "/repos")
      }
      def nextUri(conf: GithubConf, resource: ResourceMeta[GithubConf], next: Pagination[ResourceMeta[GithubConf], GithubPagination]) = next match {
        case NormalPage(page) => Some(page.uri)
        case _ => None
      }
    }
  }
}
