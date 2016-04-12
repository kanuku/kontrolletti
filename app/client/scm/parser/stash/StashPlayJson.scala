package client.scm.parser.stash

import client.scm.stash.stashmodel.{StashConf, StashPagination}
import client.scm.scmmodel._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.language.implicitConversions

trait StashPlayJson {

  /** this reads will fail, if the value of nextPageStart is null */
  implicit val stashPaginationReads: Reads[StashPagination] =
    (JsPath \ "nextPageStart").read[Int].map(StashPagination)

  implicit val stashOrgMetaReads: Reads[OrgMeta] =
    (JsPath \ "key").read[String].map(OrgMeta.apply _)

  implicit def stashRepoMetaReads(orgMeta: OrgMeta): Reads[RepoMeta] = (
    (JsPath \ "slug").read[String] and
    Reads.pure(orgMeta)
  )(RepoMeta.apply _)

  implicit def stashCommitMetaReads(repoMeta: RepoMeta): Reads[CommitMeta] = (
    (JsPath \ "id").read[String] and
    Reads.pure(repoMeta) and
    (JsPath \ "parents" \\ "id").read[List[String]]
  )(CommitMeta.apply _)
}
