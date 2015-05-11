package v1.endpoint

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
import v1.model._
import play.api.Logger
import v1.service.Search
import play.api.libs.json.Json

@Api(value = "/v1/committers", description = "Committer information")
@Singleton
class CommitterWS @Inject() (searchService: Search) extends Controller with JsonParserGithub {

  @ApiOperation(
    nickname = "get",
    value = "Returns the committer identified by the given name in the given resource",
    notes = "A committer represents an entity that pushed changes to repository.",
    httpMethod = "GET",
    //responseContainer = "List",
    response = classOf[Author])
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Operation succeeded!"),
    new ApiResponse(code = 404, message = "Did not find any resources!")))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "repo", value = "Url of the repository", required = false, dataType = "string", paramType = "query")))
  def get(repo: String) = Action.async {
    searchService.committers(repo).map { response =>
      Logger.info("WSResult " + response)
      Ok(Json.prettyPrint(Json.toJson(response))).as("application/json")
    }

  }

}