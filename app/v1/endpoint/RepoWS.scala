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
import play.api.libs.json._
import play.api.Logger
import play.api.mvc.Action
import play.api.mvc.Controller
import v1.service.Search
import v1.model.Commit
import play.utils.UriEncoding

@Api(value = "/api/v1/repos", description = "Access repository information.")
@Singleton
class RepoWS @Inject() (searchService: Search) extends Controller {

  import v1.model.KontrollettiToJsonParser._

  @ApiOperation(
    nickname = "get" //
    , value = "Get list of commits" //
    , notes = "A commit is a record of the change(s) in a repository", httpMethod = "GET" //
    , response = classOf[Commit] //
    , responseContainer = "List" //
    )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Operation succeeded."),
    new ApiResponse(code = 404, message = "Did not find the resource.")))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "repo_url", value = "repo url", required = true, dataType = "string", paramType = "path"), //
    new ApiImplicitParam(name = "valid", value = "State of spec validation", allowableValues = "true,false", required = false, dataType = "string", paramType = "query"), //
    new ApiImplicitParam(name = "from_commit_id", value = "Starting from commit-id", required = false, dataType = "string", paramType = "query"), //
    new ApiImplicitParam(name = "to_commit_id", value = "Untill commit-id", required = false, dataType = "string", paramType = "query")))
  def get(repoUrl: String, valid: Option[Boolean], from_commit_id: Option[String]) = Action.async { //, to_commit_id: Option[String]) = Action {
    val repository = UriEncoding.decodePath(repoUrl, "UTF-8")
    searchService.commits(repository).map { response =>
      if (response.isLeft)
        BadRequest(response.left.get)
      else
        Ok(Json.prettyPrint(Json.toJson(response.right.get))).as("application/json")
    }
  }
}