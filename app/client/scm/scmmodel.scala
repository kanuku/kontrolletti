package client.scm

import scalaz.{\/, Free, Monad}

import java.net.URI

object scmmodel {

  sealed trait Resource[A] { def id: String }
  final case class Commit[A : Scm](id: String, repo: Repo[A], parentIds: List[String]) extends Resource[A]
  final case class Repo[A : Scm](id: String, org: Org[A]) extends Resource[A]
  final case class Org[A : Scm](id: String) extends Resource[A]
  final case class AllCommits[A : Scm](repo: Repo[A]) extends Resource[A] { val id = "" }
  final case class AllRepos[A : Scm](org: Org[A]) extends Resource[A] { val id = "" }

  sealed trait Pagination[Res, Repr]
  // workaround for invariant
  final case class FirstPage[A : Scm, C]() extends Pagination[Resource[A], C]
  final case class LastPage[A : Scm, C]() extends Pagination[Resource[A], C]
  // type C is concrete type for pagination in different SCM
  final case class NormalPage[A : Scm, C](page: C) extends Pagination[Resource[A], C]

  final case class PagedResource[A : Scm, C](resources: Vector[Resource[A]], next: Pagination[Resource[A], C])
}
