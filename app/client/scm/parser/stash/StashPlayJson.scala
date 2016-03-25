package client.scm.parser.stash

import client.scm.stash.{StashConf, StashPagination}
import client.scm.scmmodel._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.language.implicitConversions

trait StashPlayJson {

  /** this reads will fail, if the value of nextPageStart is null */
  implicit val stashPaginationReads: Reads[StashPagination] =
    (JsPath \ "nextPageStart").read[Int].map(StashPagination)

  implicit val stashOrgMetaReads: Reads[OrgMeta[StashConf]] =
    (JsPath \ "key").read[String].map(OrgMeta[StashConf] _)

  implicit def stashRepoMetaReads(orgMeta: OrgMeta[StashConf]): Reads[RepoMeta[StashConf]] = (
    (JsPath \ "slug").read[String] and
    Reads.pure(orgMeta)
  )(RepoMeta[StashConf] _)

  implicit def stashCommitMetaReads(repoMeta: RepoMeta[StashConf]): Reads[CommitMeta[StashConf]] = (
    (JsPath \ "id").read[String] and
    Reads.pure(repoMeta) and
    (JsPath \ "parents" \\ "id").read[List[String]]
  )(CommitMeta[StashConf] _)
}
