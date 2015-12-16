package dao

case class PagedResult[T](items: Seq[T], totalCount: Int)