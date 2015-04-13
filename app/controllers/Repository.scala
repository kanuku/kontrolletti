package controllers

import elasticsearch.model.JsonModel
import play.api.mvc.Action
import play.api.mvc.Controller
import elasticsearch.Client
import play.api.libs.json._
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiResponses
import com.wordnik.swagger.annotations.ApiOperation
import com.wordnik.swagger.annotations.ApiResponse
import elasticsearch.model.Repository

@Api(value = "/repositories", description = "Operations concerning repositories")
object Repos extends Controller with JsonModel {
  @ApiOperation(
    nickname = "list",
    value = "Returns all repositories",
    notes = "A repository contains committing information of a repo",
    httpMethod = "GET",
    response = classOf[List[Repository]])
  @ApiResponses(Array(new ApiResponse(code = 200, message = "This is the list of all repositories")))
  def list = Action {
    println("Test")
    Ok(Json.prettyPrint(Json.toJson(Client.repositories)))
  }
}