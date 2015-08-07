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
import play.api.mvc.Action
import play.api.mvc.Controller
import service.Search
import model.CommitResult
import model.CommitsResult 

@Singleton
class CommitWS @Inject() (search: Search) extends Controller {
  private val defaultErrorResponse = Json.toJson(new Error("An error occurred, please check the logs", 500, "undefined"))
  val logger: Logger = Logger { this.getClass }

 def diff(host: String, project: String, repository: String, sourceId: String, targetId: String) = Action.async {
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

  def commits(host: String, project: String, repository: String, since: Option[String], until: Option[String]) = Action.async {
    logger.info("host:$host, project:$project, repository:$repository, since:$since, until:$until")
    search.commits(host, project, repository, since, until).map {
      case Left(error) =>
        logger.info("Result 500: " + error)
        InternalServerError(defaultErrorResponse).as("application/problem+json")
      case Right(None) =>
        logger.info("Result 404")
        NotFound
      case Right(Some(result)) =>
        logger.info("Result 200"+result)
        Ok(Json.toJson(new CommitsResult(List(), result))).as("application/x.zalando.commit+json")
    }
  }

  def byId(host: String, project: String, repository: String, id: String) = Action.async {
    logger.info("host:$host, project:$project, repository:$repository, since:$since, until:$until")
    search.commit(host, project, repository, id).map {
      case Left(error) =>
        logger.info("Result 500: " + error)
        InternalServerError(defaultErrorResponse).as("application/problem+json")
      case Right(None) =>
        logger.info("Result 404")
        NotFound
      case Right(Some(result)) =>
        logger.info("Result 200") 
        Ok(Json.toJson(new CommitResult(List(), result(0)))).as("application/x.zalando.commit+json")
    }

  }
}

