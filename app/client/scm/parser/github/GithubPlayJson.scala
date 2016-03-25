package client.scm.parser.github

import client.scm.github.GithubConf
import client.scm.scmmodel._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.language.implicitConversions

trait GithubPlayJson {

  implicit val gitHubOrgMetaReads: Reads[OrgMeta[GithubConf]] =
    (JsPath \ "login").read[String].map(login => OrgMeta(login))

  implicit def githubRepoMetaReads(orgMeta: OrgMeta[GithubConf]): Reads[RepoMeta[GithubConf]] = (
    (JsPath \ "name").read[String] and
    Reads.pure(orgMeta)
  )(RepoMeta[GithubConf] _)

  implicit def githubCommitMetaReads(repoMeta: RepoMeta[GithubConf]): Reads[CommitMeta[GithubConf]] = (
    (JsPath \ "sha").read[String] and
    Reads.pure(repoMeta) and
    (JsPath \ "parents").read[List[String]]
  )(CommitMeta[GithubConf] _)
}
