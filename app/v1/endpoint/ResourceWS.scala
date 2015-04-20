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

import v1.service.SearchService
import javax.inject._

@Api(value = "/v1/scm", description = "Central store for repositories")
@Singleton
class ResourceWS @Inject() (searchService: SearchService) extends Controller with JsonModel {

//  @ApiOperation(
//    nickname = "scm",
//    value = "Returns a list of all SCM's",
//    //notes = "A resource is the target from where the information is pulled",
//    httpMethod = "GET",
//    response = classOf[List[String]])
//  @ApiResponses(Array(new ApiResponse(code = 200, message = "Operation succeeded!")))
//  def list = Action {
//    Ok(Json.prettyPrint(Json.toJson(searchService.resources))).as("application/json")
//  }
//
//  @ApiOperation(
//    nickname = "get",
//    value = "Returns the resource identified by the given name",
//    notes = "A resource represents a central store where the information is stored.",
//    httpMethod = "GET",
//    response = classOf[List[Commit]])
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "Operation succeeded!"),
//    new ApiResponse(code = 404, message = "Did not find any resources!")))
//  def scm(
//    @ApiParam(value = "SCM name")@PathParam("scm") scm: String) = Action {
//    Clients.resource(scm) match {
//      case null   => NotFound
//      case result => Ok(Json.prettyPrint(Json.toJson(result))).as("application/json")
//    }
//  }

}