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

  import model.KontrollettiToModelParser._
  val logger: Logger = Logger { this.getClass }

  @ApiOperation(
    notes = "Not normalized repository-url's will result in a redirect(301) to the normalized one" //
    , value = "Access repository's meta information" //
    , httpMethod = "HEAD" //
    )
  @ApiResponses(Array(
     new ApiResponse(code = 301, message = "The `repoUrl` is not normalized, follow the redirect for to its normalized URI.") //
     , new ApiResponse(code = 400, message = "Request could not be understood, due to malformed syntax.") //
    , new ApiResponse(code = 404, message = "The 'repoUrl' is normalized, but its resource cannot be found.") //
    , new ApiResponse(code = 500, message = "Internal Server Error.")))
  @ApiImplicitParams(Array( //
    new ApiImplicitParam(name = "repositoryUrl", value = "normalized url of the repository", required = true, dataType = "string", paramType = "path")))
  def normalize(repositoryUrl: String) = Action.async {
    val url = UriEncoding.decodePath(repositoryUrl, "UTF-8")
    logger.info(s"Request: $url")

    searchService.parse(url) match {
      case Left(error) =>
        logger.info("Result: 400:" + error)
        Future.successful(BadRequest)

      case Right((host, project, repo)) =>
        val normalizedUrl = searchService.normalize(host, project, repo)

        searchService.repoExists(host, project, repo).map {
          case Right(result) if result =>

            logger.info(s"Result: 301 $normalizedUrl")
            MovedPermanently(routes.RepoWS.byUrl(URLEncoder.encode(normalizedUrl, "UTF-8")).url)

          case Left(error) =>
            logger.warn(s"Result: 500 $normalizedUrl")
            InternalServerError

          case Right(result) =>
            logger.info(s"Result: 404 $normalizedUrl")
            NotFound
        }
    }
  }
  @ApiOperation(
    notes = "Fetches the Repository object for the specified URI." //
    , value = "Fetches the Repository object for the specified URI." //
    , httpMethod = "GET" //
    )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Retrieved the object successfully.") //
    , new ApiResponse(code = 404, message = "No objects could be found matching the specified parameters.") //
    , new ApiResponse(code = 400, message = "Request could not be understood, due to malformed syntax.") //
    , new ApiResponse(code = 500, message = "Internal Server Error.")))
  @ApiImplicitParams(Array( //
    new ApiImplicitParam(name = "repositoryUrl", value = "normalized url of the repository", required = true, dataType = "string", paramType = "path")))
  def byUrl(repositoryUrl: String) = Action.async {
    val url = UriEncoding.decodePath(repositoryUrl, "UTF-8")
    logger.info(s"Request: $url")

    searchService.parse(url) match {
      case Right((host, project, repo)) =>
        searchService.repos(host, project, repo).map {
          case Right(result) if result.isEmpty =>
            logger.info(s"Result: 404 ")
            NotFound
          case Right(result) =>
            logger.info(s"Result: 200 ")
            Ok(Json.toJson(result(0))).as("application/x.zalando.repository+json")
          case Left(error) =>
            logger.info(s"Result: 500 ")
            logger.warn(error)
            InternalServerError
        }
      case Left(error) =>
        logger.info(s"Result: 400 $error")
        Future.successful(BadRequest(error))
    }

  }

}