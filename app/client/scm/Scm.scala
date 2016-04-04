package client.scm

import java.net.URI

import client.scm.scmmodel._

import scalaz.{Free, \/, EitherT}

trait Scm[A] {
  import Scm.ConfError

  type PaginationRepr
  def apiBase(conf: A): ConfError \/ URI
  def webUiBase(conf: A): ConfError \/ URI
  def accessToken(conf: A): ConfError \/ String
  def user(conf: A): ConfError \/ String
  def resourceUri(conf: A, resource: ResourceMeta[A]): ConfError \/ URI
  def nextUri(conf: A, resource: ResourceMeta[A], next: Pagination[ResourceMeta[A], PaginationRepr]): EitherT[Option, ConfError, URI]
}
object Scm {
  import ScmOps._

  type ScmOpsIO[A] = Free[ScmOps, A]
  type ConfError = String

  def checkExist[A: Scm](conf: A, res: ResourceMeta[A]): ScmOpsIO[Boolean] =
    Free.liftF[ScmOps, Boolean](CheckExist(conf, res))

  def get[A: Scm, Repr](conf: A, id: String): ScmOpsIO[Resource[A, Repr]] =
    Free.liftF[ScmOps, Resource[A, Repr]](Get(conf, id))

  def getMulti[A: Scm, C, Repr](conf: A, from: ResourceMeta[A], start: Pagination[ResourceMeta[A], C]): ScmOpsIO[PagedResource[A, C, Repr]] =
    Free.liftF(GetMulti(conf, from, start))

  def getAll[A: Scm, C, Repr](conf: A, from: ResourceMeta[A]): ScmOpsIO[PagedResource[A, C, Repr]] = {
    def go(paged: PagedResource[A, C, Repr]): ScmOpsIO[PagedResource[A, C, Repr]] =
      paged match {
        case PagedResource(res, LastPage) => Free.point(paged)
        case PagedResource(res, next) => getMulti[A, C, Repr](conf, from, next) flatMap { case PagedResource(res1, next1) =>
          go(PagedResource(res ++ res1, next1))
        }
      }
    getMulti(conf, from, FirstPage) flatMap go
  }
}

sealed trait ScmOps[A]
object ScmOps {
  final case class CheckExist[A : Scm](conf: A, res: ResourceMeta[A]) extends ScmOps[Boolean] { val scm = implicitly[Scm[A]] }
  final case class Get[A : Scm, Repr](conf: A, id: String) extends ScmOps[Resource[A, Repr]]  { val scm = implicitly[Scm[A]] }
  final case class GetMulti[A : Scm, Repr, C](conf: A, from: ResourceMeta[A], start: Pagination[ResourceMeta[A], C]) extends ScmOps[PagedResource[A, C, Repr]]  { val scm = implicitly[Scm[A]] }
}
