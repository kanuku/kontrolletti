package client.scm.stash

import org.http4s.Uri
import org.joda.time.DateTime
import argonaut._, Argonaut._
import client.scm.Scm
import Scm.{ScmUser, Token}
import client.scm.scmmodel._

import scalaz.\/
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
      def user(conf: StashConf) = ScmUser(conf.user).right
      def resourceUri(conf: StashConf, resource: ResourceMeta) = resource match {
        case OrgMeta(id) =>  apiBase(conf).map(_ / "projects" / s"$id")
        case RepoMeta(id, org) => resourceUri(conf, org).map(_ / "repos" / s"$id")
        case CommitMeta(id, repo, _) => resourceUri(conf, repo).map(_ / "commits" / s"$id")
        case AllCommitsMeta(repo) => resourceUri(conf, repo).map(_ / "commits")
        case AllReposMeta(org) => resourceUri(conf, org).map(_ / "repos")
      }
    }
  }

  // port from SCMParser
  final case class StashCommit(run: model.Commit) extends AnyVal

  final case class StashRepo(run: model.Repository) extends AnyVal
  object StashRepo {
    val repoDecodeJson: DecodeJson[model.Repository] = ???
  }

  final case class StashAuthor(run: model.Author) extends AnyVal
  object StashAuthor {
    val authorDecodeJson: DecodeJson[model.Author] =
      DecodeJson(c => for {
        name  <- (c --\ "name").as[String]
        email <- (c --\ "emailAddress").as[String]
      } yield model.Author(
        name  = name,
        email = email,
        links = None
      ))
    implicit val stashAuthorDecodeJson: DecodeJson[StashAuthor] =
      authorDecodeJson.map(StashAuthor.apply)
  }

  final case class StashDateTime(run: DateTime) extends AnyVal
  object StashDateTime {
    implicit val stashDateTimeDecdeJson: DecodeJson[StashDateTime] =
      DecodeJson.optionDecoder(
        x => for {
          num <- x.number
          ts  <- num.toLong
          dt  <- \/.fromTryCatchNonFatal(new DateTime(ts)).toOption
        } yield StashDateTime(dt)
      , "timestamp")
  }
}
