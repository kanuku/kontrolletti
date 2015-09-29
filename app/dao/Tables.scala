package dao

import org.joda.time.DateTime

import model.Author
import model.Commit
import model.KontrollettiToJsonParser
import model.KontrollettiToModelParser
import model.Link
import model.Repository
import model.Ticket
import play.api.db.slick.HasDatabaseConfig
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue
import utility.Transformer
object Tables { self: HasDatabaseConfigProvider[KontrollettiPostgresDriver] =>

  import Transformer._
  import dao.KontrollettiPostgresDriver.api._

  import KontrollettiToJsonParser.linkWriter
  import KontrollettiToModelParser.linkReader

   private  val schema = Some("kont_data")
  
  lazy val authors = TableQuery[AuthorTable]
  lazy val tickets = TableQuery[TicketTable]
  lazy val repositories = TableQuery[RepositoryTable]
  lazy val commits = TableQuery[CommitTable]

  /**
   * TICKET TABLE
   */
  class TicketTable(tag: Tag) extends Table[Ticket](tag, schema, "TICKETS") {

    def name = column[String]("name")
    def href = column[String]("href", O.PrimaryKey)
    def links = column[Option[JsValue]]("links")
    //    def commit
    def * = (name, href, links) <> ((apply _).tupled, unapply)
    def apply(name: String, href: String, links: Option[JsValue]) = new Ticket(name, href, deserializeFromOption2Option[List[Link]](links))
    def unapply(ticket: Ticket) = Some((ticket.name, ticket.href, serializeFromOption2Option(ticket.links)))
  }

  /**
   * AUTHOR TABLE
   */
  class AuthorTable(tag: Tag) extends Table[Author](tag, schema, "AUTHORS") {
    def name = column[String]("name")
    def email = column[String]("email", O.PrimaryKey)
    def links = column[Option[JsValue]]("links")
    def * = (name, email, links) <> ((apply _).tupled, unapply)

    def apply(name: String, email: String, jasonBourne: Option[JsValue]) = new Author(name, email, deserializeFromOption2Option[List[Link]](jasonBourne))
    def unapply(author: Author): Option[(String, String, Option[JsValue])] = Some((author.name, author.email, serializeFromOption2Option(author.links)))

  }

  /**
   * REPOSITORY TABLE
   */
  class RepositoryTable(tag: Tag) extends Table[Repository](tag, schema, "REPOSITORIES") {
    def url = column[String]("url", O.PrimaryKey)
    def host = column[String]("host")
    def project = column[String]("project")
    def repository = column[String]("repository")
    def synch = column[Boolean]("is_synchronizable")
    def lastSynched = column[Option[DateTime]]("synchronized_at")
    def lastFailed = column[Option[DateTime]]("last_failed_at")
    def links = column[Option[JsValue]]("links")
    def * = (url, host, project, repository, synch, lastSynched, lastFailed, links) <> ((apply _).tupled, unapply)

    def apply(url: String, host: String, project: String, repository: String, synch: Boolean, lastSynched: Option[DateTime], lastFailed: Option[DateTime], links: Option[JsValue]): Repository = new Repository(url, host, project, repository, synch, lastSynched, lastFailed, deserializeFromOption2Option[List[Link]](links))
    def unapply(repo: Repository): Option[(String, String, String, String, Boolean, Option[DateTime], Option[DateTime], Option[JsValue])] = Some((repo.url, repo.host, repo.project, repo.repository, repo.enabled, repo.lastSync, repo.lastFailed, serializeFromOption2Option(repo.links)))
  }

  /**
   * COMMIT TABLE
   */
  class CommitTable(tag: Tag) extends Table[Commit](tag, schema, "COMMITS") {
    def id = column[String]("id", O.PrimaryKey)
    def parentIds = column[Option[List[String]]]("parent_id")
    def date = column[DateTime]("date")
    def repoURL = column[String]("repository_url")
    def jsonValue = column[JsValue]("json_value")

    def * = (id, repoURL, parentIds, date, jsonValue) <> ((apply _).tupled, unapply)

    def repoFK = foreignKey("repository_url", repoURL, repositories)(_.url)

    def apply(id: String, repoId: String, parentIds: Option[List[String]], date: DateTime, jsonValue: JsValue): Commit = deserialize(jsonValue)(KontrollettiToModelParser.commitReader)
    def unapply(commit: Commit): Option[(String, String, Option[List[String]], DateTime, JsValue)] = Some((commit.id, commit.repoUrl, commit.parentIds, commit.date, serialize(commit)(KontrollettiToJsonParser.commitWriter)))
  }

}

