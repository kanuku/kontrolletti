package dao

import org.joda.time.DateTime

case class PagedResult[T](items: Seq[T], totalCount: Int)
case class RepoParameters(val host: String, val project: String, //
                          val repository: String)
case class FilterParameters(val since: Option[String] = None, //
                            val until: Option[String] = None, //
                            val valid: Option[Boolean] = None, //
                            val sinceDate: Option[DateTime] = None, //
                            val untilDate: Option[DateTime] = None //
)
case class DateFilterParams(
  val since: Option[DateTime],
  val until: Option[DateTime],
  val valid: Option[Boolean]
)

case class PageParameters(
  val pageNumber: Option[Int] = None, //
  val perPage: Option[Int] = None)
