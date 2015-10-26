package endpoint

import scala.concurrent.Future
import javax.inject._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action
import play.api.mvc.Controller
import model._
import play.api.Logger
import play.api.libs.json.Json
import model.KontrollettiToJsonParser._
import model.KontrollettiToModelParser._
import dao.CommitRepository

@Singleton
class TicketWS @Inject() (commitRepo: CommitRepository) extends Controller {
  private val logger: Logger = Logger(this.getClass())
  def tickets(host: String, project: String, repository: String, sinceId: Option[String], untilId: Option[String]) = Action.async {
    logger.info(s"host: $host, project: $project repository: $repository, sinceId: $sinceId, untilId: $untilId")
    commitRepo.tickets(host, project, repository, since = sinceId, until = untilId).map {
      _.toList match {
        case Nil =>
          logger.info(s"Result: 404 ")
          NotFound
        case commits =>
          logger.info(s"Result: 200 ")
          val result = (commits.flatMap { commit => commit.tickets }).flatMap { x => x }
          Ok(Json.toJson(result)).as("application/x.zalando.ticket+json")

      }
    }
  }
}