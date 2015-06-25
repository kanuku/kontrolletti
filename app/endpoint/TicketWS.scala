package endpoint

import scala.concurrent.Future
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiImplicitParam
import com.wordnik.swagger.annotations.ApiImplicitParams
import com.wordnik.swagger.annotations.ApiOperation
import com.wordnik.swagger.annotations.ApiResponse
import com.wordnik.swagger.annotations.ApiResponses
import javax.inject._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action
import play.api.mvc.Controller
import model._
import play.api.Logger
import service.Search
import play.api.libs.json.Json

@Api(value = "/api/hosts", description = "Committer information")
@Singleton
class TicketWS @Inject() (searchService: Search) extends Controller {

  import model.KontrollettiToModelParser._

  private val logger: Logger = Logger(this.getClass())
  
  @ApiOperation(
    nickname = "get",
    value = "Fetches all the Ticket objects for the specified repository.",
    notes = "A committer represents an entity that pushed changes to repository.",
    httpMethod = "GET",
    //responseContainer = "List",
    response = classOf[Author])
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Operation succeeded!"),
    new ApiResponse(code = 404, message = "Did not find any resources!")))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "repo", value = "Url of the repository", required = false, dataType = "string", paramType = "query")))
  def tickets(host: String, project: String, repository: String, sinceId: Option[String], untilId: Option[String]) = Action.async {
    
    logger.info(s"host: $host, project: $project repository: $repository, sinceId: $sinceId, untilId: $untilId")
    val result= searchService.tickets(host, project, repository, sinceId, untilId)
    logger.info("Information: "+result)
    result.map {
      case Right(None) =>
        logger.info(s"Result: 404 ")
        NotFound
      case Right(result) =>
        logger.info(s"Result: 200 ")
        Ok(Json.toJson(result)).as("application/x.zalando.ticket+json")
      case Left(error) =>
        logger.info("Result:500")
        InternalServerError.as("application/problem+json")
    }
  }

}