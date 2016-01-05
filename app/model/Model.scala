package model

import org.joda.time.DateTime

import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.json.__
/**
 * The models
 *
 */

case class Error(detail: String, status: Int, errorType: String)
case class Link(href: String, method: String, rel: String, relType: String)
case class Author(name: String, email: String, links: Option[List[Link]])

//TODO: Add [specs] and [valid] properties
case class Commit(id: String, message: String, parentIds: Option[List[String]], author: Author, tickets: Option[List[Ticket]], valid: Option[Boolean], links: Option[List[Link]], date: DateTime, repoUrl: String)
case class Repository(url: String, host: String, project: String, repository: String, enabled: Boolean, lastSync: Option[DateTime], lastFailed: Option[DateTime], links: Option[List[Link]])
case class Ticket(name: String, href: String, links: Option[List[Link]])

//MUST HAVE HATEOAS RESULTS
//FIXME: Create a generic Parent case class. This way you will only need one single writer :) for all Results.
case class RepositoryResult(links: List[Link], result: Repository)
case class RepositoriesResult(links: List[Link], result: List[Repository])
case class TicketResult(links: List[Link], result: List[Ticket])
case class CommitResult(links: List[Link], result: Commit)
case class CommitsResult(links: List[Link], result: List[Commit])

//

//TODO: Evaluate Moving the readers in this parser(KontrollettiToJsonParser) into Companion objects
// And overriding those companion objects in the SCM Parser

object KontrollettiToModelParser {
  val dateTimeISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  val dateReads = Reads.jodaDateReads(dateTimeISO8601)

  implicit val errorReader: Reads[Error] = (
    (__ \ "detail").read[String] and
    (__ \ "status").read[Int] and
    (__ \ "errorType").read[String] //
    )(Error.apply _)

  implicit val linkReader: Reads[Link] = (
    (__ \ "href").read[String] and
    (__ \ "method").read[String] and
    (__ \ "rel").read[String] and
    (__ \ "relType").read[String] //
    )(Link.apply _)

  implicit val authorReader: Reads[Author] = (
    (__ \ "name").read[String] and
    (__ \ "email").read[String] and
    (__ \ "links").readNullable[List[Link]] //
    )(Author.apply _)

  implicit val ticketReader: Reads[Ticket] = (
    (__ \ "name").read[String] and
    (__ \ "href").read[String] and
    (__ \ "links").readNullable[List[Link]] //
    )(Ticket.apply _)

  implicit val commitReader: Reads[Commit] = (
    (__ \ "id").read[String] and
    (__ \ "message").read[String] and
    (__ \ "parent_ids").readNullable[List[String]] and
    (__ \ "author").read[Author] and
    (__ \ "tickets").readNullable[List[Ticket]] and
    (__ \ "valid").readNullable[Boolean] and
    (__ \ "links").readNullable[List[Link]] and //
    (__ \ "date").read[DateTime](dateReads) and //
    (__ \ "repository_url").read[String] //
    )(Commit.apply _)

  implicit val repositoryReader: Reads[Repository] = (
    (__ \ "url").read[String] and
    (__ \ "host").read[String] and
    (__ \ "project").read[String] and
    (__ \ "repository").read[String] and
    (__ \ "enabled").read[Boolean] and
    (__ \ "last_synchronized").readNullable[DateTime](dateReads) and //
    (__ \ "last_failed").readNullable[DateTime](dateReads) and //
    (__ \ "links").readNullable[List[Link]] //
    )(Repository.apply _)

  implicit val commitsResultReader: Reads[CommitsResult] = (
    (__ \ "_links").read[List[Link]] and
    (__ \ "result").read[List[Commit]])(CommitsResult.apply _)

  implicit val commitResultReader: Reads[CommitResult] = (
    (__ \ "_links").read[List[Link]] and
    (__ \ "result").read[Commit])(CommitResult.apply _)

  implicit val repositoriesResultReader: Reads[RepositoriesResult] = (
    (__ \ "_links").read[List[Link]] and
    (__ \ "result").read[List[Repository]])(RepositoriesResult.apply _)

  implicit val repositoryResultReader: Reads[RepositoryResult] = (
    (__ \ "_links").read[List[Link]] and
    (__ \ "result").read[Repository])(RepositoryResult.apply _)

}
object KontrollettiToJsonParser {

  implicit val dateWrites = Writes.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

  implicit val errorWriter: Writes[Error] = (
    (__ \ "detail").write[String] and
    (__ \ "status").write[Int] and
    (__ \ "errorType").write[String] //
    )(unlift(Error.unapply))

  implicit val linkWriter: Writes[Link] = (
    (__ \ "href").write[String] and
    (__ \ "method").write[String] and
    (__ \ "rel").write[String] and
    (__ \ "relType").write[String] //
    )(unlift(Link.unapply))

  implicit val authorWriter: Writes[Author] = (
    (__ \ "name").write[String] and
    (__ \ "email").write[String] and
    (__ \ "links").writeNullable[List[Link]] //
    )(unlift(Author.unapply))

  implicit val ticketWriter: Writes[Ticket] = (
    (__ \ "name").write[String] and
    (__ \ "href").write[String] and
    (__ \ "links").writeNullable[List[Link]] //
    )(unlift(Ticket.unapply))

  implicit val commitWriter: Writes[Commit] = (
    (__ \ "id").write[String] and
    (__ \ "message").write[String] and
    (__ \ "parent_ids").writeNullable[List[String]] and
    (__ \ "author").write[Author] and
    (__ \ "tickets").writeNullable[List[Ticket]] and
    (__ \ "valid").writeNullable[Boolean] and
    (__ \ "links").writeNullable[List[Link]] and //
    (__ \ "date").write[DateTime](dateWrites) and //
    (__ \ "repository_url").write[String])(unlift(Commit.unapply))

  implicit val repositoryWriter: Writes[Repository] = (
    (__ \ "url").write[String] and
    (__ \ "host").write[String] and
    (__ \ "project").write[String] and
    (__ \ "repository").write[String] and
    (__ \ "enabled").write[Boolean] and
    (__ \ "last_synchronized").writeNullable[DateTime](dateWrites) and //
    (__ \ "last_failed").writeNullable[DateTime](dateWrites) and //
    (__ \ "links").writeNullable[List[Link]] //
    )(unlift(Repository.unapply))

  implicit val commitsResultWriter: Writes[CommitsResult] = (
    (__ \ "_links").write[List[Link]] and //
    (__ \ "result").write[List[Commit]])(unlift(CommitsResult.unapply))

  implicit val commitResultWriter: Writes[CommitResult] = (
    (__ \ "_links").write[List[Link]] and //
    (__ \ "result").write[Commit])(unlift(CommitResult.unapply))

  implicit val repositoryResultWriter: Writes[RepositoryResult] = (
    (__ \ "_links").write[List[Link]] and //
    (__ \ "result").write[Repository])(unlift(RepositoryResult.unapply))
  implicit val repositoriesResultWriter: Writes[RepositoriesResult] = (
    (__ \ "_links").write[List[Link]] and //
    (__ \ "result").write[List[Repository]])(unlift(RepositoriesResult.unapply))

}
