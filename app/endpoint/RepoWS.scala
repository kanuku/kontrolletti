package endpoint

import scala.concurrent.Future
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiImplicitParam
import com.wordnik.swagger.annotations.ApiImplicitParams
import com.wordnik.swagger.annotations.ApiOperation
import com.wordnik.swagger.annotations.ApiResponse
import com.wordnik.swagger.annotations.ApiResponses
import javax.inject._
import model.Commit
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.utils.UriEncoding
import service.Search
import java.net.URLEncoder
import scala.concurrent.future
import scala.concurrent.Future

@Api(value = "/api/repos", description = "Access repository information.")
@Singleton
class RepoWS @Inject() (searchService: Search) extends Controller {
  val NORMALIZED_REQUEST_PARAMETER = "Normalized-Repository-Identifier"
  import model.KontrollettiToJsonParser._
  val logger: Logger = Logger { this.getClass }

  private val acceptableCodes = List(200, 301)

  @ApiOperation(
    nickname = "get" //
    , value = "Get list of commits" //
    , notes = "A commit is a record of the change(s) in a repository", httpMethod = "GET" //
    , response = classOf[Commit] //
    , responseContainer = "List" //
    )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Operation succeeded.") //
    , new ApiResponse(code = 404, message = "Did not find the resource.") //
    , new ApiResponse(code = 400, message = "Bad request.") //
    ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "repo_url", value = "repo url", required = true, dataType = "string", paramType = "path") //
    , new ApiImplicitParam(name = "valid", value = "State of spec validation", allowableValues = "true,false", required = false, dataType = "string", paramType = "query") //
    , new ApiImplicitParam(name = "from_commit_id", value = "Starting from commit-id", required = false, dataType = "string", paramType = "query") //
    , new ApiImplicitParam(name = "to_commit_id", value = "Untill commit-id", required = false, dataType = "string", paramType = "query")) //
    )
  def commits(repoUrl: String, valid: Option[Boolean], from_commit_id: Option[String]) = Action.async {
    val repository = UriEncoding.decodePath(repoUrl, "UTF-8")
    logger.info(s"Request: $repository")

    Future.firstCompletedOf(Seq(searchService.commits(repository))).map {
      case Left(error) =>
        logger.warn(error)
        BadRequest(error)
      case Right(response) =>
        logger.info("Result: OK")
        Ok(Json.prettyPrint(Json.toJson(response))).as("application/json")
    }
  }

  @ApiOperation(
    nickname = "head", //
    value = "Access repository's meta information" //
    , httpMethod = "HEAD" //
    )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Url is a normalized repository-url.") //
    , new ApiResponse(code = 301, message = "Moved permanently!") //
    , new ApiResponse(code = 404, message = "Did not find the resource.") //
    , new ApiResponse(code = 400, message = "Bad request.") //
    ))
  @ApiImplicitParams(Array( //
    new ApiImplicitParam(name = "repo_url", //
      value = "repo url", //
      required = true, //
      dataType = "string", //
      paramType = "path")))
  def normalize(repoUrl: String) = Action.async {
    val repository = UriEncoding.decodePath(repoUrl, "UTF-8")
    logger.info(s"Request: $repository")

    searchService.normalizeURL(repository) match {
      case Left(error) =>
        logger.warn(s"Result: 400 $error")
        Future.successful(BadRequest)

      case Right(url) if (repository.equals(url)) => searchService.repoExists(url).map {
        case Right(result) if acceptableCodes.contains(result) =>
          logger.info(s"Result>>: 200 $url")
          Ok

        case Right(result) if result == 500 =>
          logger.info(s"Result: 500 $url")
          InternalServerError
          
        case Right(result) =>
          logger.info(s"Result: 404 $url")
          NotFound
      }

      case Right(url) =>
        logger.info(s"Result: 301 $url")
        Future.successful(MovedPermanently(routes.RepoWS.get(URLEncoder.encode(url, "UTF-8")).url) //
          .withHeaders(NORMALIZED_REQUEST_PARAMETER -> url))
    }
  }

  def get(repoUrl: String) = Action { NotImplemented }

}