package dao

import scala.concurrent.Future
import javax.inject.Inject
import javax.inject.Singleton
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import java.sql.SQLException
import model.Ticket

trait TicketRepository {

  def initializeDatabase: Future[Unit]
  def save(authors: List[Ticket]): Future[Unit]
  def list(): Future[Seq[Ticket]]
  def ticketByUrl(url: String): Future[Option[Ticket]]

}

@Singleton
class TicketRepositoryImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends TicketRepository with HasDatabaseConfigProvider[KontrollettiPostgresDriver] {
  import dao.KontrollettiPostgresDriver.api._
  import utility.FutureUtil._
  val logger: Logger = Logger(this.getClass())
  private val tickets = Tables.tickets

  def initializeDatabase: Future[Unit] = {
    logger.info("Started initializing Tickets table")
    db.run { tickets.schema.create }.map { x =>
      logger.info("Fisnihed initializing Tickets table")
    }
  }

  def save(input: List[Ticket]): Future[Unit] = {
    logger.info("Saving author")
    handleError(db.run(tickets ++= input).map(_ => ()))
  }

  def ticketByUrl(url: String): Future[Option[Ticket]] = db.run(tickets.filter(_.href === url).result.headOption)

  def list(): Future[Seq[Ticket]] = db.run(tickets.result)

}

