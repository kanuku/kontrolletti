package client.scm

object scmmodel {

  final case class Resource[A, Repr](meta: ResourceMeta[A], resource: Repr)

  sealed trait ResourceMeta[A] { def id: String }
  final case class CommitMeta[A : Scm](id: String, repo: RepoMeta[A], parentIds: List[String]) extends ResourceMeta[A]
  final case class RepoMeta[A : Scm](id: String, org: OrgMeta[A]) extends ResourceMeta[A]
  final case class OrgMeta[A : Scm](id: String) extends ResourceMeta[A]
  final case class AllCommitsMeta[A : Scm](repo: RepoMeta[A]) extends ResourceMeta[A] { val id = "" }
  final case class AllReposMeta[A : Scm](org: OrgMeta[A]) extends ResourceMeta[A] { val id = "" }

  sealed trait Pagination[+Res, +PaginationRepr]
  case object FirstPage extends Pagination[Nothing, Nothing]
  case object LastPage extends Pagination[Nothing, Nothing]
  final case class NormalPage[A : Scm, PaginationRepr](page: PaginationRepr)
    extends Pagination[ResourceMeta[A], PaginationRepr]

  final case class PagedResource[A : Scm, PaginationRepr, Repr](resources: Vector[Resource[A, Repr]], next: Pagination[ResourceMeta[A], PaginationRepr])
}
