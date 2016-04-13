package client.scm
package github

import client.scm.Scm
import Scm.{ConfError, Token}
import client.scm.scmmodel._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import argonaut._
import Argonaut._

import scalaz.{\/, \/-, EitherT}
import scalaz.std.option._
import scalaz.syntax.either._
import scalaz.syntax.monad._
import scalaz.syntax.std.option._

object githubmodel {

  final case class GithubConf(apiBase: Uri, webUiBase: Uri, accessToken: Token)
  final case class GithubPagination(uri: Uri, rel: String)
  object GithubPagination {
    import fastparse.all._
    private val whiteSpacesParser: Parser[Unit] = P(CharIn(" \t\n") | StringIn("\r\n"))
    private val genericUriParser: Parser[Uri] =
      P("<" ~ CharPred(_ != '>').rep.! ~ ">").flatMap { str =>
        Uri.fromString(str) match {
          case \/-(uri) => Pass.map(_ => uri)
          case _ => Fail
        }
      }
    private val relParser: Parser[String] = P("rel=\"" ~ CharPred(_ != '"').rep.! ~ "\"")
    private val genericLinkRelParser: Parser[(Uri, String)] =
      P(whiteSpacesParser.rep ~ genericUriParser ~ whiteSpacesParser.rep ~ ";" ~ whiteSpacesParser ~ relParser ~ ",".? ~ whiteSpacesParser.rep)
    val nextUriParser: Parser[Option[Uri]] =
      genericLinkRelParser.rep.map(_.filter({ case (_, rel) =>
        rel == "next"
      }).headOption.map(_._1))
  }

  object GithubConf {

    implicit val githubScm: Scm[GithubConf] = new Scm[GithubConf] {
      type PaginationRepr = GithubPagination
      def apiBase(conf: GithubConf) = conf.apiBase.right
      def webUiBase(conf: GithubConf) = conf.webUiBase.right
      def accessToken(conf: GithubConf) = conf.accessToken.right
      def user(conf: GithubConf) = "Github does not require any user".left
      def resourceUri(conf: GithubConf, resource: ResourceMeta) = resource match {
        case OrgMeta(id) => apiBase(conf).map(_ / "orgs" / s"$id")
        case RepoMeta(id, org) => apiBase(conf).map(_ / s"repos" / s"${org.id}" / s"$id")
        case CommitMeta(id, repo, _) => resourceUri(conf, repo).map(_ / "commits" / s"$id")
        case AllCommitsMeta(repo) => resourceUri(conf, repo).map(_ / "commits")
        case AllReposMeta(org) => resourceUri(conf, org).map(_ / "repos")
      }
      def paginationUri(conf: GithubConf, resource: ResourceMeta, page: Pagination[GithubPagination]) = page match {
        case NormalPage(p) =>
          p.uri.some.right
        case _ => None.right
      }
    }
  }


  // direct translation of old play-json reads
  // some of them could be improved

  final case class GithubCommit(run: model.Commit) extends AnyVal
  object GithubCommit {

    final case class Parent(url: String, sha: String)
    implicit val parentDecodeJson: DecodeJson[Parent] =
      jdecode2L(Parent.apply)("url", "sha")

    val commitDecodeJson: DecodeJson[model.Commit] =
      DecodeJson(c => for {
        id <- (c --\ "sha").as[String]
        message <- (c --\ "commit" --\ "message").as[String]
        author <- (c --\ "commit" --\ "committer").as[GithubAuthor]
        parents <- (c --\ "parents").as[List[Parent]]
        datetime <- (c --\ "commit" --\ "author" --\ "date").as[GithubDateTime]
      } yield model.Commit(
        id        = id,
        message   = message,
        parentIds = Some(parents.map(_.sha)),
        author    = author.run,
        tickets   = None,
        valid     = None,
        links     = None,
        date      = datetime.run,
        repoUrl   = ""
      ))

    implicit val githubcommitDecodeJson: DecodeJson[GithubCommit] =
      commitDecodeJson.map(GithubCommit.apply)
  }

  final case class GithubRepo(run: model.Repository) extends AnyVal
  object GithubRepo {
    val repoDecodeJson: DecodeJson[model.Repository] =
      DecodeJson(c => (c --\ "html_url").as[String] map { url =>
        model.Repository(
          url        = url,
          host       = "",
          project    = "",
          repository = "",
          enabled    = true,
          lastSync   = None,
          lastFailed = None,
          links      = None
        )
      })
    implicit val githubRepoDecodeJson: DecodeJson[GithubRepo] =
      repoDecodeJson.map(GithubRepo.apply)
  }

  final case class GithubAuthor(run: model.Author) extends AnyVal
  object GithubAuthor {
    val authorDecodeJson: DecodeJson[model.Author] =
      DecodeJson(c => for {
        name  <- (c --\ "name").as[String]
        email <- (c --\ "email").as[String]
      } yield model.Author(name, email, None))
    implicit val githubAuthorDecodeJson: DecodeJson[GithubAuthor] =
      authorDecodeJson.map(GithubAuthor.apply)
  }

  final case class GithubDateTime(run: DateTime) extends AnyVal
  object GithubDateTime {
    val githubDTformat = "yyyy-MM-dd'T'HH:mm:ssZ"
    val githubDTformatter = DateTimeFormat.forPattern(githubDTformat)
    implicit val githubDateTimeDecodeJson: DecodeJson[GithubDateTime] =
      DecodeJson.optionDecoder(
        x => x.string flatMap { str =>
          \/.fromTryCatchNonFatal(githubDTformatter.parseDateTime(str))
            .map(GithubDateTime.apply)
            .toOption
        }, s"Format: $githubDTformat")
  }
}
