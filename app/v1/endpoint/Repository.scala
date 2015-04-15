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

@Api(value = "/1.0/repos", description = "Endpoint for requesting imformation about repositories")
object Repository extends Controller with JsonModel {
  import v1.model.Repository
  @ApiOperation(
    nickname = "repositories",
    value = "Returns all repositories",
    httpMethod = "GET",
    response = classOf[List[Repository]])
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Operation succeeded!")))
  def list = Action {
    Ok(Json.prettyPrint(Json.toJson(Client.repositories))).as("application/json")
  }

  @ApiOperation(
    nickname = "get",
    value = "Returns the repository known by the given name at the given resource in the given group",
    notes = "A repository is a central place where data is stored and maintained.",
    httpMethod = "GET",
    response = classOf[Repository])
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Operation succeeded!"),
    new ApiResponse(code = 404, message = "Repository with the given id was not found!")))
  def get(@ApiParam(value = "Repository name")@PathParam("repo") repo: String,
          @ApiParam(value = "Resource name")@PathParam("resource") resource: String,
           @ApiParam(value = "Group name")@PathParam("group") group: String) = Action {
    println("Request" + repo + " - " + resource)
    Client.repository(repo, resource) match {
      case Nil    => NotFound
      case result => Ok(Json.prettyPrint(Json.toJson(result))).as("application/json")

    }
  }

} 