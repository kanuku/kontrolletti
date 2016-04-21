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

  object StashCommit {

    final case class Parent(id: String, displayId: String)
    object Parent {
      implicit val parentDecodeJson: DecodeJson[Parent] =
        jdecode2L(Parent.apply)("id", "displayId")
    }
    val commitDecodeJson: DecodeJson[model.Commit] =
      DecodeJson(c => for {
        id      <- (c --\ "id").as[String]
        message <- (c --\ "message").as[String]
        author  <- (c --\ "author").as[StashAuthor]
        date    <- (c --\ "authorTimestamp").as[StashDateTime]
        parents <- (c --\ "parents").as[List[Parent]]
      } yield model.Commit(
        id        = id,
        message   = message,
        parentIds = Some(parents.map(_.id)),
        author    = author.run,
        date      = date.run,
        tickets   = None,
        valid     = None,
        links     = None,
        repoUrl   = ""
      ))
  }

  final case class StashRepo(run: model.Repository) extends AnyVal

  object StashRepo {
    import scalaz.NonEmptyList
    import utility.json.scalazinstances._

    final case class Href(href: String)
    object Href {
      implicit val decodeHref: DecodeJson[Href] =
        jdecode1L(Href.apply)("href")
    }

    val repoDecodeJson: DecodeJson[model.Repository] =
      DecodeJson(c => for {
        hrefs <- (c --\ "links" --\ "self").as[NonEmptyList[Href]]
      } yield model.Repository(
        url = hrefs.head.href,
        host = "",
        project = "",
        repository = "",
        true,
        None,
        None,
        None
      ))

    implicit val stashRepoDecodeJson: DecodeJson[StashRepo] =
      repoDecodeJson.map(StashRepo.apply)
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
