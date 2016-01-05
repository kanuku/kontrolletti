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
import dao.RepoParameters
import dao.RepoParameters
import dao.PageParameters

@Singleton
class TicketWS @Inject() (commitRepo: CommitRepository) extends Controller with CommitFilterValidator {
  private val logger: Logger = Logger(this.getClass())
  def tickets(host: String, project: String, repository: String, since: Option[String], until: Option[String], page: Option[Int], perPage: Option[Int], fromDate: Option[String], toDate: Option[String]) = Action.async {
    logger.info(s"host: $host, project: $project repository: $repository, since: $since, until: $until, page: $page, perPage: $perPage, fromDate: $fromDate, toDate: $toDate")
    validate(since, until, None, fromDate, toDate)(BadRequest.header.status, INVALID_INPUT) match {
      case Left(error) => Future { BadRequest(Json.toJson(error)) }
      case Right(filterParameters) =>
        val repo = RepoParameters(host, project, repository)
        val pagination = PageParameters(page, perPage)
        commitRepo.tickets(repo, filterParameters, pagination).map {
          case PagedResult(Nil, _) =>
            logger.info(s"Result: 404 ")
            NotFound
          case PagedResult(tickets, total) =>
            logger.info(s"Result: 200 ")
            Ok(Json.toJson(tickets)).as("application/x.zalando.ticket+json")
              .withHeaders(X_TOTAL_COUNT -> String.valueOf(total))
        }
    }
  }
}