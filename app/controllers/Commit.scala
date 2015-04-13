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



@Api(value = "/commits", description = "Endpoint regarding commit-id's information")
object Commit extends Controller with JsonModel {
  import model.Commit
  @ApiOperation(
    nickname = "Commits",
    value = "Returns a list of imported commits",
    notes = "A commit is a record of the change(s) in a repository",
    httpMethod = "GET",
    response = classOf[List[Commit]])
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Operation succeeded!")))
  def list = Action {
    Ok(Json.prettyPrint(Json.toJson(Client.commits))).as("application/json")
  }
}