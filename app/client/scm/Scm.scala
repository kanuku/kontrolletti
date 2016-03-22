package client.scm

import scmmodel._

import scalaz.{\/, Free, Monad}

import java.net.URI

trait Scm[A] {
  type ConfError = String
  type PaginationRepr
  def apiBase(conf: A): ConfError \/ URI
  def webUiBase(conf: A): ConfError \/ URI
  def accessToken(conf: A): ConfError \/ String
  def user(conf: A): ConfError \/ String
  def resourceUri(conf: A, resource: Resource[A]): URI
  def nextUri(conf: A, resource: Resource[A], next: Pagination[Resource[A], PaginationRepr]): Option[URI]
}
object Scm {
  import ScmOps._

  type ScmOpsIO[A] = Free[ScmOps, A]

  def checkExist[A: Scm](conf: A, res: Resource[A]): ScmOpsIO[Boolean] =
    Free.liftF[ScmOps, Boolean](CheckExist(conf, res))

  def get[A: Scm](conf: A, id: String): ScmOpsIO[Resource[A]] =
    Free.liftF[ScmOps, Resource[A]](Get(conf, id))

  def getMulti[A: Scm, C](conf: A, from: Resource[A], start: Pagination[Resource[A], C]): ScmOpsIO[PagedResource[A, C]] =
    Free.liftF(GetMulti(conf, from, start))

  def getAll[A: Scm, C](conf: A, from: Resource[A]): ScmOpsIO[PagedResource[A, C]] = {
    def go(paged: PagedResource[A, C]): ScmOpsIO[PagedResource[A, C]] =
      paged match {
        case PagedResource(res, LastPage()) => Free.point(paged)
        case PagedResource(res, next) => getMulti(conf, from, next) flatMap {
          case PagedResource(res1, next1) => go(PagedResource(res ++ res1, next1))
        }
      }
    getMulti(conf, from, FirstPage[A, C]()) flatMap go
  }
}

sealed trait ScmOps[A]
object ScmOps {
  final case class CheckExist[A : Scm](conf: A, res: Resource[A]) extends ScmOps[Boolean]
  final case class Get[A : Scm](conf: A, id: String) extends ScmOps[Resource[A]]
  final case class GetMulti[A : Scm, C](conf: A, from: Resource[A], start: Pagination[Resource[A], C]) extends ScmOps[PagedResource[A, C]]
}
