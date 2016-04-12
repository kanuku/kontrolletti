package client.scm

import client.scm.scmmodel._

import scalaz.{Free, \/, EitherT}
import scalaz.syntax.all._

trait Scm[A] {
  import Scm.{ConfError, Token}

  type PaginationRepr
  def apiBase(conf: A): ConfError \/ Uri
  def webUiBase(conf: A): ConfError \/ Uri
  def accessToken(conf: A): ConfError \/ Token
  def user(conf: A): ConfError \/ String
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
  final case class ReqParams(uri: Uri, token: Token)

  def checkExist[A](conf: A, res: ResourceMeta)(implicit scm: Scm[A]): ScmOpsIO[Boolean] = {
    for {
      uri    <- scm.resourceUri(conf, res)         |> ScmOpsIO.fromDisjunction
      token  <- scm.accessToken(conf)              |> ScmOpsIO.fromDisjunction
      exists <- Free.liftF(CheckExist(uri, token)) |> ScmOpsIO.liftF
    } yield exists
  }

  def getRepo[A](conf: A, res: ResourceMeta)(implicit scm: Scm[A]): ScmOpsIO[model.Repository] =
    for {
      uri   <- scm.resourceUri(conf, res)      |> ScmOpsIO.fromDisjunction
      token <- scm.accessToken(conf)           |> ScmOpsIO.fromDisjunction
      repo  <- Free.liftF(GetRepo(uri, token)) |> ScmOpsIO.liftF
    } yield repo

  def getCommit[A: Scm](conf: A, res: ResourceMeta)(implicit scm: Scm[A]): ScmOpsIO[model.Commit] =
    for {
      uri    <- scm.resourceUri(conf, res)        |> ScmOpsIO.fromDisjunction
      token  <- scm.accessToken(conf)             |> ScmOpsIO.fromDisjunction
      commit <- Free.liftF(GetCommit(uri, token)) |> ScmOpsIO.liftF
    } yield commit

  def getAllCommits[A](conf: A, res: ResourceMeta, start: Option[Uri])(implicit scm: Scm[A]): ScmOpsIO[Vector[model.Commit]] = {
    val paged = for {
      token        <- scm.accessToken(conf)                   |> ScmOpsIO.fromDisjunction
      initial      <- scm.resourceUri(conf, res)              |> ScmOpsIO.fromDisjunction
      uri          <- start.getOrElse(initial)                |> ScmOpsIO.pure
      pagedCommits <- Free.liftF(GetCommitsPaged(uri, token)) |> ScmOpsIO.liftF
    } yield pagedCommits

    paged flatMap { pr: PagedResource[model.Commit] =>
      pr.next match {
        case Some(next) => getAllCommits(conf, res, Some(next)).map(pr.resources ++ _)
        case None       => ScmOpsIO.pure(pr.resources)
      }
    }
  }
}

sealed trait ScmOps[A]
object ScmOps {
  import Scm.Token
  // TODO: GetCommit GetRepo ...
  final case class CheckExist(uri: Uri, token: Token) extends ScmOps[Boolean]
  final case class GetCommit(uri: Uri, token: Token) extends ScmOps[model.Commit]
  final case class GetRepo(uri:Uri, token: Token) extends ScmOps[model.Repository]
  final case class GetCommitsPaged(uri: Uri, token: Token) extends ScmOps[PagedResource[model.Commit]]
}
