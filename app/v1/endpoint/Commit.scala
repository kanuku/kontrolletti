package v1.endpoint

import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.json._
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiOperation
import com.wordnik.swagger.annotations.ApiResponses
import com.wordnik.swagger.annotations.ApiResponse
import v1.model.JsonModel
import v1.client._
import com.wordnik.swagger.annotations.ApiParam
import javax.ws.rs.PathParam

@Api(value = "/1.0/commits", description = "Endpoint for requesting commit-id's information")
object Commit extends Controller with JsonModel {

  import v1.model._

  @ApiOperation(
    nickname = "commits",
    value = "Returns a list of imported commits",
    notes = "A commit is a record of the change(s) in a repository",
    httpMethod = "GET",
    response = classOf[List[Commit]])
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Operation succeeded!")))
  def list = Action {
    Ok(Json.prettyPrint(Json.toJson(Client.commits))).as("application/json")
  }

  @ApiOperation(
    nickname = "get",
    value = "Returns the commit of the given commit-id in the given resource",
    notes = "A commit is a record of the change(s) in a repository",
    httpMethod = "GET",
    response = classOf[List[Commit]])
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Operation succeeded!"),
    new ApiResponse(code = 404, message = "Did not find any resources!")))
  def get(@ApiParam(value = "Commit id")@PathParam("id") id: String,
          @ApiParam(value = "Resource name")@PathParam("resource") resource: String,
          @ApiParam(value = "Group name")@PathParam("group") group: String,
          @ApiParam(value = "Repository name")@PathParam("repo") repo: String) = Action {
    Client.commit(id, resource) match {
      case Nil    => NotFound
      case result => Ok(Json.prettyPrint(Json.toJson(result))).as("application/json")
    }
  }
}