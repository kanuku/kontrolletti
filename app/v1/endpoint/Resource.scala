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

@Api(value = "/1.0/scm", description = "Central store where users can push changes to.")
object Resource extends Controller with JsonModel {

  @ApiOperation(
    nickname = "resources",
    value = "Returns a list of the known resources",
    //notes = "A resource is the target from where the information is pulled",
    httpMethod = "GET",
    response = classOf[List[String]])
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Operation succeeded!")))
  def list = Action {
    Ok(Json.prettyPrint(Json.toJson(Client.resources))).as("application/json")
  }

  @ApiOperation(
    nickname = "get",
    value = "Returns the resource identified by the given name",
    notes = "A resource represents a central store where the information is stored.",
    httpMethod = "GET",
    response = classOf[List[Commit]])
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Operation succeeded!"),
    new ApiResponse(code = 404, message = "Did not find any resources!")))
  def getByGroup(
    @ApiParam(value = "Resource name")@PathParam("resource") resource: String,
    @ApiParam(value = "Group name")@PathParam("group") group: String) = Action {
    Client.resource(resource) match {
      case null   => NotFound
      case result => Ok(Json.prettyPrint(Json.toJson(result))).as("application/json")
    }
  }
  @ApiOperation(
    nickname = "get",
    value = "Returns the resource identified by the given name",
    notes = "A resource represents a central store where the information is stored.",
    httpMethod = "GET",
    response = classOf[List[Commit]])
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Operation succeeded!"),
    new ApiResponse(code = 404, message = "Did not find any resources!")))
  def getByUser(
    @ApiParam(value = "Resource name")@PathParam("resource") resource: String,
    @ApiParam(value = "User name")@PathParam("user") user: String) = Action {
    Client.resource(resource) match {
      case null   => NotFound
      case result => Ok(Json.prettyPrint(Json.toJson(result))).as("application/json")
    }
  }

}