package client.scm

import client.scm.scmmodel._
import org.http4s.Request

import scalaz.{Free, \/, EitherT, Show}
import scalaz.syntax.all._

trait Scm[A] {
  import Scm.{ConfError, Token, ScmUser}

  type PaginationRepr
  def apiBase(conf: A): ConfError \/ Uri
  def webUiBase(conf: A): ConfError \/ Uri
  def accessToken(conf: A): ConfError \/ Token
  def user(conf: A): ConfError \/ ScmUser
  def resourceUri(conf: A, resource: ResourceMeta): ConfError \/ Uri
}

object Scm {
  import ScmOps._
  import client.scm.scmmodel._

  type ScmOpsIO[A] = EitherT[Free[ScmOps, ?], String, A]

  object ScmOpsIO {
    def pure[A](a: A): ScmOpsIO[A] = liftF(a.point[Free[ScmOps, ?]])
    def fromDisjunction[A](va: String \/ A): ScmOpsIO[A] =
      EitherT.fromDisjunction[Free[ScmOps, ?]].apply(va)
    def liftF[A](fa: Free[ScmOps, A]): ScmOpsIO[A] =
      fa.liftM[EitherT[?[_], String, ?]]
  }
  type ConfError = String

  final case class Token(token: String) extends AnyVal
  // TODO: move to client.oauth package
  final case class BearerToken(bToken: String) extends AnyVal
  object BearerToken {
    implicit val showInstance: Show[BearerToken] = new Show[BearerToken] {
      override def shows(t: BearerToken): String = s"Bearer ${t.bToken}"
    }
  }
  final case class ScmUser(user: String) extends AnyVal
  object ScmUser {
    implicit val showInstance: Show[ScmUser] = new Show[ScmUser] {
      override def shows(u: ScmUser): String = s"ScmUser: ${u.user}"
    }
  }
  final case class ReqParams(uri: Uri, token: Token)

  def buildRequest[A](conf: A, res: ResourceMeta, init: Option[Uri])(implicit scm: Scm[A]): ScmOpsIO[Request] =
    BuildRequest(conf, scm, res, init) |> Free.liftF[ScmOps, Request] |> ScmOpsIO.liftF

  def checkExist[A](conf: A, res: ResourceMeta)(implicit scm: Scm[A]): ScmOpsIO[Boolean] =
    buildRequest(conf, res, None) flatMap { req =>
      CheckExist(req) |> Free.liftF |> ScmOpsIO.liftF
    }

  def getRepo[A](conf: A, res: RepoMeta)(implicit scm: Scm[A]): ScmOpsIO[model.Repository] =
    buildRequest(conf, res, None) flatMap { req =>
      GetRepo(req) |> Free.liftF |> ScmOpsIO.liftF
    }

  def getCommit[A](conf: A, res: CommitMeta)(implicit scm: Scm[A]): ScmOpsIO[model.Commit] = buildRequest(conf, res, None) flatMap { req =>
      GetCommit(req) |> Free.liftF |> ScmOpsIO.liftF
    }

  def getAllCommits[A](conf: A, res: AllCommitsMeta, start: Option[Uri])(implicit scm: Scm[A]): ScmOpsIO[Vector[model.Commit]] = {
    val paged: ScmOpsIO[PagedResult[model.Commit]] =
      buildRequest(conf, res, start) flatMap { req =>
        GetCommitsPaged(req) |> Free.liftF |> ScmOpsIO.liftF
      }

    paged flatMap { pr: PagedResult[model.Commit] =>
      pr.next match {
        case Some(next) => getAllCommits(conf, res, Some(next)).map(pr.results ++ _)
        case None       => ScmOpsIO.pure(pr.results)
      }
    }
  }
}

sealed trait ScmOps[A]
object ScmOps {
  final case class BuildRequest[A](conf: A, scm: Scm[A], res: ResourceMeta, init: Option[Uri]) extends ScmOps[Request]
  final case class CheckExist(request: Request)                             extends ScmOps[Boolean]
  final case class GetCommit(request: Request)                              extends ScmOps[model.Commit]
  final case class GetRepo(request: Request)                                extends ScmOps[model.Repository]
  final case class GetCommitsPaged(request: Request)                        extends ScmOps[PagedResult[model.Commit]]
}
