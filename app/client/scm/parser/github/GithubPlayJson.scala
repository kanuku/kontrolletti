package client.scm.parser.github

import org.joda.time.DateTime
import client.scm.github.githubmodel.GithubConf
import client.scm.scmmodel._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.language.implicitConversions

object GithubPlayJson {

  implicit val gitHubOrgMetaReads: Reads[OrgMeta] =
    (JsPath \ "login").read[String].map(login => OrgMeta(login))

  implicit def githubRepoMetaReads(orgMeta: OrgMeta): Reads[RepoMeta] = (
    (JsPath \ "name").read[String] and
    Reads.pure(orgMeta)
  )(RepoMeta.apply _)

  implicit def githubCommitMetaReads(repoMeta: RepoMeta): Reads[CommitMeta] = (
    (JsPath \ "sha").read[String] and
    Reads.pure(repoMeta) and
    (JsPath \ "parents").read[List[String]]
  )(CommitMeta.apply _)
}
