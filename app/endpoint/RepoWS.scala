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

  import model.KontrollettiToModelParser._
  val logger: Logger = Logger { this.getClass }


  @ApiOperation(
    notes = "Not normalized repository-url's will result in a redirect(301) to the normalized one" //
    , value = "Access repository's meta information" //
    , httpMethod = "HEAD" //
    )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "URI is correct and can be accessed if necessary.") //
    , new ApiResponse(code = 301, message = "Moved permanently!") //
    , new ApiResponse(code = 404, message = "Did not find the resource.") //
    , new ApiResponse(code = 400, message = "Bad request.") //
    , new ApiResponse(code = 500, message = "Internal Server Error.")))
  @ApiImplicitParams(Array( //
    new ApiImplicitParam(name = "repositoryUrl", value = "normalized url of the repository", required = true, dataType = "string", paramType = "path")))
  def normalize(repositoryUrl: String) = Action.async {

    val repository = UriEncoding.decodePath(repositoryUrl, "UTF-8")

    logger.info(s"Request: $repository")

    searchService.parse(repository) match {
      
      case Left(error) => Future.successful(BadRequest)
      
      case Right((host, project, repo)) =>
        val normalizedUrl = searchService.normalizeURL(host, project, repo)
        if (repository.equals(normalizedUrl)) {
          val acceptableCodes = List(200, 301)
          searchService.repoExists(host, project, repo).map {
            case Right(result) if acceptableCodes.contains(result) =>
              logger.info(s"Result>>: 200 $normalizedUrl")
              Ok
            case Right(result) if result == 500 =>
              logger.info(s"Result: 500 $normalizedUrl")
              InternalServerError

            case Right(result) =>
              logger.info(s"Result: 404 $normalizedUrl")
              NotFound
          }
        } else {
          logger.info(s"Result: 301 $normalizedUrl")
          Future.successful(MovedPermanently(routes.RepoWS.byUrl(URLEncoder.encode(normalizedUrl, "UTF-8")).url) //
            .withHeaders(NORMALIZED_REQUEST_PARAMETER -> normalizedUrl))
        }
    }
  }

 
  def byUrl(repositoryUrl: String) = Action{NotImplemented}

}