package v2.endpoint

import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.json._
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiOperation
import com.wordnik.swagger.annotations.ApiResponses
import com.wordnik.swagger.annotations.ApiResponse
import v1.client._
import v1.model.JsonModel

@Api(value = "/2.0/repositories", description = "Access to repositories")
object Repository extends Controller with JsonModel {
  import v1.model.Repository
  @ApiOperation(
    nickname = "Repositories",
    value = "Returns all repositories",
    notes = "A repository contains committing information of a repo",
    httpMethod = "GET",
    response = classOf[List[Repository]])
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Operation succeeded!")))
  def list = Action {
    Ok(Json.prettyPrint(Json.toJson(Client.repositories))).as("application/json")
  }
} 