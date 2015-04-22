package v1.endpoint

import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiOperation
import com.wordnik.swagger.annotations.ApiResponse
import com.wordnik.swagger.annotations.ApiResponses
import javax.inject._
import play.api.libs.json._
import play.api.mvc.Action
import play.api.mvc.Controller 
import v1.service.Search
import com.wordnik.swagger.annotations.ApiImplicitParams
import com.wordnik.swagger.annotations.ApiParam
import com.wordnik.swagger.annotations.ApiImplicitParam
import javax.ws.rs.PathParam
import play.api.mvc.BodyParsers.parse
import v1.model._
import play.Logger
@Api(value = "/v1/commits", description = "Information regarding the changes in a repository.")
@Singleton
class CommitWS @Inject() (searchService: Search) extends Controller  {

  @ApiOperation(
    nickname = "get",
    value = "Returns the commit of the given commit-id in the given scm",
    notes = "A commit is a record of the change(s) in a repository",
    httpMethod = "GET",
    response = classOf[String])
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Operation succeeded!"),
    new ApiResponse(code = 404, message = "Did not find any resources!")))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "repo", value = "Url of the repository", required = false, dataType = "string", paramType = "query")))
  def get(repo: String) = Action {
    Logger.debug(s"Request $repo ")
    //        searchService.commit(repo) match {
    //          case null   => NotFound
    //          case result => Ok(Json.prettyPrint(Json.toJson(result))).as("application/json")
    //        }
    Ok(" >> <<")
  }
}