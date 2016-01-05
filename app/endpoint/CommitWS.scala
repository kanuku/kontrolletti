package endpoint

import java.net.URLEncoder
import scala.concurrent.Future
import javax.inject._
import model.KontrollettiToJsonParser._
import model.Error
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.mvc.Action
import play.api.mvc.Controller
import service.Search
import model.CommitResult
import model.CommitsResult
import dao.CommitRepository
import dao.PagedResult
import dao.RepoParameters
import dao.PageParameters

@Singleton
class CommitWS @Inject() (search: Search, commitRepo: CommitRepository) extends Controller with CommitFilterValidator {
  private val defaultErrorResponse = Json.toJson(new Error("An error occurred, please check the logs", 500, "undefined"))
  val logger: Logger = Logger { this.getClass }

  def diff(host: String, project: String, repository: String, sourceId: String, targetId: String) = Action.async {
    logger.info(s"Request(byId) - host:$host, project:$project, repository:$repository, from: $sourceId, to: $targetId")
    search.diff(host, project, repository, sourceId, targetId).map {
      case Left(error) =>
        logger.info("Result 500: " + error)
        InternalServerError.as("application/problem+json")
      case Right(None) =>
        logger.info("Result 404")
        NotFound

      case Right(Some(link)) =>
        logger.info("Result 301: " + link.href)
        Redirect(URLEncoder.encode(link.href, "UTF-8"))
    }
  }

  def commits(host: String, project: String, repository: String, since: Option[String], until: Option[String], isValid: Option[Boolean], page: Option[Int], perPage: Option[Int], fromDate: Option[String], toDate: Option[String]) = Action.async {
    logger.info(s"Request(commits) - host:$host, project:$project, repository:$repository, since:$since, until:$until, isValid:$isValid, page $page, perPage $perPage, fromDate: $fromDate, toDate: $toDate")
    validate(since, until, isValid, fromDate, toDate)(BadRequest.header.status, INVALID_INPUT) match {
      case Left(error) => Future { BadRequest(Json.toJson(error)) }
      case Right(filterParameters) =>
        val repo = RepoParameters(host, project, repository)
        val pagination = PageParameters(page, perPage)
        commitRepo.get(repo, filterParameters, pagination).map {
          case PagedResult(Nil, _) =>
            logger.info("Result 404")
            NotFound
          case PagedResult(result, total) =>
            logger.info("Result 200")
            Ok(Json.toJson(new CommitsResult(List(), result.toList))).as("application/x.zalando.commit+json") //
              .withHeaders(X_TOTAL_COUNT -> String.valueOf(total))
        }
    }
  }

  def byId(host: String, project: String, repository: String, id: String) = Action.async {
    logger.info(s"Request(byId) - host:$host, project:$project, repository:$repository")
    commitRepo.byId(RepoParameters(host, project, repository), id).map {
      case None =>
        logger.info("result")
        NotFound
      case Some(result) =>
        logger.info("Result 200")
        Ok(Json.toJson(new CommitResult(List(), result))).as("application/x.zalando.commit+json")
    }

  }
}
