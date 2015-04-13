package controllers

import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.json._
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiOperation
import com.wordnik.swagger.annotations.ApiResponses
import com.wordnik.swagger.annotations.ApiResponse
import model._
import client._

@Api(value = "/committers", description = "Endpoint regarding author information")
object Committer extends Controller with JsonModel {
  import model.Committer

  @ApiOperation(
    nickname = "Authors",
    value = "Returns a list of known authors",
    notes = "The author is the entity who originally wrote the code.",
    httpMethod = "GET",
    response = classOf[List[Committer]])
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Operation succeeded!")))
  def list = Action {
    Ok(Json.prettyPrint(Json.toJson(Client.committers))).as("application/json")
  }
}