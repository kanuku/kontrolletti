package client.scm

object scmmodel {

  sealed trait ResourceMeta { def id: String }
  final case class CommitMeta(id: String, repo: RepoMeta, parentIds: List[String]) extends ResourceMeta
  final case class RepoMeta(id: String, org: OrgMeta) extends ResourceMeta
  final case class OrgMeta(id: String) extends ResourceMeta { type Repr = Int }
  final case class AllCommitsMeta(repo: RepoMeta) extends ResourceMeta { val id = "" }
  final case class AllReposMeta(org: OrgMeta) extends ResourceMeta { val id = "" }

  sealed trait Pagination[+PaginationRepr]
  case object FirstPage extends Pagination[Nothing]
  case object LastPage extends Pagination[Nothing]
  final case class NormalPage[PaginationRepr](page: PaginationRepr)
      extends Pagination[PaginationRepr]
  final case class PagedResult[A](results: Vector[A], next: Option[Uri])
}
