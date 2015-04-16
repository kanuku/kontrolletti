package v1.endpoint

import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.json._
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiOperation
import com.wordnik.swagger.annotations.ApiResponses
import com.wordnik.swagger.annotations.ApiResponse
import v1.model._
import v1.client._
import com.wordnik.swagger.annotations.ApiParam
import javax.ws.rs.PathParam

@Api(value = "/v1/users", description = "User information")
object User extends Controller with JsonModel {
  import v1.model.User

  @ApiOperation(
    nickname = "users",
    value = "Returns a list of known uers",
    notes = "The user is the main entity responsible for making the changes in the different resources.",
    httpMethod = "GET",
    response = classOf[List[User]])
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Operation succeeded!")))
  def list = Action {
    Ok(Json.prettyPrint(Json.toJson(Clients.users))).as("application/json")
  }

  @ApiOperation(
    nickname = "get",
    value = "Returns the user identified by the given name in the given resource",
    notes = "A resource represents a central store where the information is stored.",
    httpMethod = "GET",
    response = classOf[List[Commit]])
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Operation succeeded!"),
    new ApiResponse(code = 404, message = "Did not find any resources!")))
  def get(@ApiParam(value = "Name of the user")@PathParam("resource") name: String,
    @ApiParam(value = "Name of the scm")@PathParam("scm") scm: String) = Action {
    Clients.committer(name, scm) match {
      case null   => NotFound
      case result => Ok(Json.prettyPrint(Json.toJson(result))).as("application/json")
    }
  }

}