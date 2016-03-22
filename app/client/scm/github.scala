package client.scm

import scmmodel._

import scalaz.syntax.either._
import java.net.URI

object github {

  final case class GithubConf(apiBase: URI, webUiBase: URI, accessToken: String)
  final case class GithubPagination(uri: URI, rel: String)

  val githubScm: Scm[GithubConf] = new Scm[GithubConf] {
    type PaginationRepr = GithubPagination
    def apiBase(conf: GithubConf) = conf.apiBase.right
    def webUiBase(conf: GithubConf) = conf.webUiBase.right
    def accessToken(conf: GithubConf) = conf.accessToken.right
    def user(conf: GithubConf) = "Github does not require any user".left
    def resourceUri(conf: GithubConf, resource: Resource[GithubConf]): URI = resource match {
      case Org(id) => URI.create(apiBase(conf).toString + s"/repos/$id")
      case Repo(id, org) => URI.create(resourceUri(conf, org).toString + s"/commits/$id")
      case Commit(id, repo, _) => URI.create(resourceUri(conf, repo).toString + s"/$id")
      case AllCommits(repo) => URI.create(resourceUri(conf, repo).toString + s"/commits")
      case AllRepos(org) => URI.create(resourceUri(conf, org).toString + "/repos")
    }
    def nextUri(conf: GithubConf, resource: Resource[GithubConf], next: Pagination[Resource[GithubConf], GithubPagination]) = next match {
      case NormalPage(page) => Some(page.uri)
      case _ => None
    }
  }
}
