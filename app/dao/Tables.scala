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
import utility.GeneralHelper
import Transformer._
import dao.KontrollettiPostgresDriver.api._

import KontrollettiToJsonParser.linkWriter
import KontrollettiToModelParser.linkReader

object Tables extends GeneralHelper { self: HasDatabaseConfigProvider[KontrollettiPostgresDriver] =>

  private val schema = Some("kont_data")

  lazy val repositories = TableQuery[RepositoryTable]
  lazy val commits = TableQuery[CommitTable]

  /**
   * REPOSITORY TABLE
   */
  class RepositoryTable(tag: Tag) extends Table[Repository](tag, schema, "repositories") {
    def url = column[String]("url", O.PrimaryKey)
    def host = column[String]("host")
    def project = column[String]("project")
    def repository = column[String]("repository")
    def synch = column[Boolean]("is_synchronizable")
    def lastSynched = column[Option[DateTime]]("synchronized_at")
    def lastFailed = column[Option[DateTime]]("last_failed_at")
    def links = column[Option[JsValue]]("links")

    def * = (url, host, project, repository, synch, lastSynched, lastFailed, links) <> ((apply _).tupled, unapply)
    def idx = index("idx_hpr", (host, project, repository), unique = true)

    def apply(url: String, host: String, project: String, repository: String, synch: Boolean, lastSynched: Option[DateTime], lastFailed: Option[DateTime], links: Option[JsValue]): Repository = new Repository(url, host, project, repository, synch, lastSynched, lastFailed, deserializeFromOption2Option[List[Link]](links))
    def unapply(repo: Repository): Option[(String, String, String, String, Boolean, Option[DateTime], Option[DateTime], Option[JsValue])] = Some((repo.url, repo.host, repo.project, repo.repository, repo.enabled, repo.lastSync, repo.lastFailed, serializeFromOption2Option(repo.links)))
  }

  /**
   * COMMIT TABLE
   */
  class CommitTable(tag: Tag) extends Table[Commit](tag, schema, "commits") {
    def id = column[String]("id", O.PrimaryKey)
    def date = column[DateTime]("date")
    def repoURL = column[String]("repository_url", O.PrimaryKey)
    def nrOfTickets = column[Int]("nr_tickets")
    def jsonValue = column[JsValue]("json_value")

    def * = (id, repoURL, date, nrOfTickets, jsonValue) <> ((apply _).tupled, unapply)
    def repoFK = foreignKey("repository_url", repoURL, repositories)(_.url)
    def pk = primaryKey("commits_pkey", (id, repoURL))

    def apply(id: String, repoId: String, date: DateTime, nrOfTickets: Int, jsonValue: JsValue): Commit = deserialize(jsonValue)(KontrollettiToModelParser.commitReader)
    def unapply(commit: Commit): Option[(String, String, DateTime, Int, JsValue)] = Some((commit.id, commit.repoUrl, commit.date, numberOfTickets(commit.tickets), serialize(commit)(KontrollettiToJsonParser.commitWriter)))

  }

}
