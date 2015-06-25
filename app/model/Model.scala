package model

import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.json._
import play.api.libs.functional.syntax._
/**
 * The models
 *
 */

case class Error(detail: String, status: Int, errorType: String)
case class Link(href: String, method: String, rel: String, relType: String)
case class Author(name: String, email: String, links: List[Link])
case class Commit(id: String, message: String, parentId: List[String], author: Author, valid: Option[Boolean], links: List[Link])
case class Repository(href: String, project: String, host: String, repository: String, commits: List[Commit], links: List[Link])
case class Ticket(name: String, description: String, href: String, links: List[Link])

class Default(_links: List[Link])
case class RepositoryResult(_links: List[Link], result: Repository) extends Default(_links)
case class RepositoriesResult(_links: List[Link], result: List[Repository]) extends Default(_links)
case class TicketResult(_links: List[Link], result: List[Ticket]) extends Default(_links)
case class CommitResult(_links: List[Link], result: Commit) extends Default(_links)
case class CommitsResult(_links: List[Link], result: List[Commit]) extends Default(_links)

//TODO: Evaluate Moving the readers in this parser(KontrollettiToJsonParser) into Companion objects
// And overriding those companion objects in the SCM Parser

object KontrollettiToJsonParser {

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
    (__ \ "links").read[List[Link]] //
    )(Author.apply _)

  implicit val commitReader: Reads[Commit] = (
    (__ \ "id").read[String] and
    (__ \ "message").read[String] and
    (__ \ "parentId").read[List[String]] and
    (__ \ "author").read[Author] and
    Reads.pure(None) and
    (__ \ "links").read[List[Link]] //
    )(Commit.apply _)

  implicit val repositoryReader: Reads[Repository] = (
    (__ \ "href").read[String] and
    (__ \ "host").read[String] and
    (__ \ "project").read[String] and
    (__ \ "repository").read[String] and
    (__ \ "commits").read[List[Commit]] and
    (__ \ "links").read[List[Link]] //
    )(Repository.apply _)

  implicit val ticketReader: Reads[Ticket] = (
    (__ \ "name").read[String] and
    (__ \ "description").read[String] and
    (__ \ "href").read[String] and
    (__ \ "links").read[List[Link]] //
    )(Ticket.apply _)

  implicit val commitsResultReader: Reads[CommitsResult] = (
    (__ \ "_links").read[List[Link]] and
    (__ \ "result").read[List[Commit]])(CommitsResult.apply _)

  implicit val commitResultReader: Reads[CommitResult] = (
    (__ \ "_links").read[List[Link]] and
    (__ \ "result").read[Commit])(CommitResult.apply _)

}
object KontrollettiToModelParser {

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
    (__ \ "links").write[List[Link]] //
    )(unlift(Author.unapply))

  implicit val commitWriter: Writes[Commit] = (
    (__ \ "id").write[String] and
    (__ \ "message").write[String] and
    (__ \ "parentId").write[List[String]] and
    (__ \ "author").write[Author] and
    (__ \ "valid").writeNullable[Boolean] and
    (__ \ "links").write[List[Link]] //
    )(unlift(Commit.unapply))

  implicit val repositoryWriter: Writes[Repository] = (
    (__ \ "href").write[String] and
    (__ \ "host").write[String] and
    (__ \ "project").write[String] and
    (__ \ "repository").write[String] and
    (__ \ "commits").write[List[Commit]] and
    (__ \ "links").write[List[Link]] //
    )(unlift(Repository.unapply))

  implicit val ticketWriter: Writes[Ticket] = (
    (__ \ "name").write[String] and
    (__ \ "description").write[String] and
    (__ \ "href").write[String] and
    (__ \ "links").write[List[Link]] //
    )(unlift(Ticket.unapply))

  implicit val commitsResultWriter: Writes[CommitsResult] = (
    (__ \ "links").write[List[Link]] and //
    (__ \ "result").write[List[Commit]])(unlift(CommitsResult.unapply))

  implicit val commitResultWriter: Writes[CommitResult] = (
    (__ \ "links").write[List[Link]] and //
    (__ \ "result").write[Commit])(unlift(CommitResult.unapply))

}



