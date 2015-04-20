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
import v1.service.SearchService
import javax.inject._

@Api(value = "/v1/commits", description = "Information regarding the changes in a repository.")
@Singleton
class CommitWS @Inject()(searchService: SearchService) extends Controller with JsonModel {

  import v1.model._

  @ApiOperation(
    nickname = "commits",
    value = "Returns a list of imported commits",
    notes = "A commit is a record of the change(s) in a repository",
    httpMethod = "GET",
    response = classOf[List[Commit]])
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Operation succeeded!")))
  def list = Action {
    //Ok(Json.prettyPrint(Json.toJson(Clients.commits))).as("application/json")
     searchService.commits match {
      case Nil    => NotFound
      case result => Ok(Json.prettyPrint(Json.toJson(result))).as("application/json")
    }   
  } 

  @ApiOperation(
    nickname = "get",
    value = "Returns the commit of the given commit-id in the given scm",
    notes = "A commit is a record of the change(s) in a repository",
    httpMethod = "GET",
    response = classOf[List[Commit]])
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Operation succeeded!"),
    new ApiResponse(code = 404, message = "Did not find any resources!")))
  def get(@ApiParam(value = "Commit id")@PathParam("id") id: String,
          @ApiParam(value = "Scm name")@PathParam("scm") scm: String,
          @ApiParam(value = "Group name")@PathParam("group") group: String,
          @ApiParam(value = "Repository name")@PathParam("repo") repo: String) = Action {
    searchService.commit(id) match {
      case null   => NotFound
      case result => Ok(Json.prettyPrint(Json.toJson(result))).as("application/json")
    }
  }
}