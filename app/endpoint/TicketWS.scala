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
import dao.PagedResult

@Singleton
class TicketWS @Inject() (commitRepo: CommitRepository) extends Controller {
  private val logger: Logger = Logger(this.getClass())
  def tickets(host: String, project: String, repository: String, sinceId: Option[String], untilId: Option[String], page: Option[Int], perPage: Option[Int]) = Action.async {
    logger.info(s"host: $host, project: $project repository: $repository, sinceId: $sinceId, untilId: $untilId")
    commitRepo.tickets(host, project, repository, since = sinceId, until = untilId, pageNumber = page, perPage = perPage).map {
      _ match {
        case PagedResult(Nil, _) =>
          logger.info(s"Result: 404 ")
          NotFound
        case PagedResult(tickets, total) =>
          logger.info(s"Result: 200 ")
          Ok(Json.toJson(tickets)).as("application/x.zalando.ticket+json") //
            .withHeaders(X_TOTAL_COUNT -> String.valueOf(total))
      }
    }
  }
}