package v1.endpoint

import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiOperation
import com.wordnik.swagger.annotations.ApiParam
import com.wordnik.swagger.annotations.ApiResponse
import com.wordnik.swagger.annotations.ApiResponses

import javax.ws.rs.PathParam
import play.api.libs.json._
import play.api.mvc.Action
import play.api.mvc.Controller
import v1.client._
import v1.model._
import v1.service.SearchService
import javax.inject._

@Api(value = "/v1/groups", description = "A group of repositories")
@Singleton
class GroupWS @Inject()(searchService: SearchService) extends Controller with JsonModel {

  @ApiOperation(
    nickname = "groups",
    value = "Returns a list of the known groups",
    notes = "A group is a number of repositories that are classed together",
    httpMethod = "GET",
    response = classOf[List[String]])
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Operation succeeded!")))
  def list = Action {
    Ok(Json.prettyPrint(Json.toJson(searchService.repos))).as("application/json")
  }

  @ApiOperation(
    nickname = "get",
    value = "Returns the group identified by the given name in the given scm",
    notes = "SCM represents a central store where the information is stored.",
    httpMethod = "GET",
    response = classOf[List[Commit]])
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Operation succeeded!"),
    new ApiResponse(code = 404, message = "Did not find any resources!")))
  def get(@ApiParam(value = "Group name")@PathParam("group") name: String,
          @ApiParam(value = "Resource name")@PathParam("scm") scm: String) = Action {
    searchService.repo(scm) match {
      case null   => NotFound
      case result => Ok(Json.prettyPrint(Json.toJson(result))).as("application/json")
    }
  }

}