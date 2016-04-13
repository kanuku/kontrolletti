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
  def paginationUri(conf: A, resource: ResourceMeta, page: Pagination[PaginationRepr]): ConfError \/ Option[Uri]
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
  final case class PagedResult[A](results: Vector[A], next: Option[Request])

  def buildRequest[A](conf: A, res: ResourceMeta)(implicit scm: Scm[A]): ScmOpsIO[Request] =
    BuildRequest(conf, scm, res) |> Free.liftF[ScmOps, Request] |> ScmOpsIO.liftF

  def checkExist[A](conf: A, res: ResourceMeta)(implicit scm: Scm[A]): ScmOpsIO[Boolean] =
    buildRequest(conf, res) flatMap { req =>
      CheckExist(req) |> Free.liftF |> ScmOpsIO.liftF
    }

  def getRepo[A](conf: A, res: RepoMeta)(implicit scm: Scm[A]): ScmOpsIO[model.Repository] =
    buildRequest(conf, res) flatMap { req =>
      GetRepo(req) |> Free.liftF |> ScmOpsIO.liftF
    }

  def getCommit[A](conf: A, res: CommitMeta)(implicit scm: Scm[A]): ScmOpsIO[model.Commit] = buildRequest(conf, res) flatMap { req =>
      GetCommit(req) |> Free.liftF |> ScmOpsIO.liftF
    }

  def getAllCommits[A](conf: A, res: AllCommitsMeta, start: Option[Request])(implicit scm: Scm[A]): ScmOpsIO[Vector[model.Commit]] = {
    val paged: ScmOpsIO[PagedResult[model.Commit]] =
      buildRequest(conf, res) flatMap { initial =>
        ???
      }
    // for {
    //   token        <- scm.accessToken(conf)                   |> ScmOpsIO.fromDisjunction
    //   initial      <- scm.resourceUri(conf, res)              |> ScmOpsIO.fromDisjunction
    //   uri          <- start.getOrElse(initial)                |> ScmOpsIO.pure
    //   pagedCommits <- Free.liftF(GetCommitsPaged(uri, token)) |> ScmOpsIO.liftF
    // } yield pagedCommits

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
  import Scm.PagedResult
  final case class BuildRequest[A](conf: A, scm: Scm[A], res: ResourceMeta) extends ScmOps[Request]
  final case class CheckExist(request: Request)                             extends ScmOps[Boolean]
  final case class GetCommit(request: Request)                              extends ScmOps[model.Commit]
  final case class GetRepo(request: Request)                                extends ScmOps[model.Repository]
  final case class GetCommitsPaged(request: Request)                        extends ScmOps[PagedResult[model.Commit]]
}
