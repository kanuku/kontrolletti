package endpoint

import scala.concurrent.Future 
import javax.inject._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action
import play.api.mvc.Controller
import model._
import play.api.Logger
import service.Search
import play.api.libs.json.Json


@Singleton
class TicketWS @Inject() (searchService: Search) extends Controller {

  import model.KontrollettiToModelParser._

  private val logger: Logger = Logger(this.getClass())
  
  def tickets(host: String, project: String, repository: String, sinceId: Option[String], untilId: Option[String]) = Action.async {
    
    logger.info(s"host: $host, project: $project repository: $repository, sinceId: $sinceId, untilId: $untilId")
    val result= searchService.tickets(host, project, repository, sinceId, untilId)
    logger.info("Information: "+result)
    result.map {
      case Right(None) =>
        logger.info(s"Result: 404 ")
        NotFound
      case Right(Some(result)) =>
        logger.info(s"Result: 200 ")
        Ok(Json.toJson(result)).as("application/x.zalando.ticket+json")
      case Left(error) =>
        logger.info("Result:500")
        InternalServerError.as("application/problem+json")
    }
  }

}