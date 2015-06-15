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

  

  
  
  
  /**
   * Fetches commits for the repository-url.
   * @param repoUrl - Repository-url
   * @return Action with the content(list of commits for the given repo)
   */
  @ApiOperation(
    value = "Get all commits from the specified repository-url" //
    , httpMethod = "GET" //
    , response = classOf[Commit] //
    , responseContainer = "List" //
    )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Operation succeeded.") //
    , new ApiResponse(code = 404, message = "Did not find the resource.") //
    , new ApiResponse(code = 400, message = "Bad Request."), //
    new ApiResponse(code = 500, message = "Internal Server Error.") //
    ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "repo_url", value = "normalized url of the repository", required = true, dataType = "string", paramType = "path") //
    ))
  def commitsByUrl(repoUrl: String) = Action.async {
    val url = UriEncoding.decodePath(repoUrl, "UTF-8")
    logger.info(s"Request: $url")
    searchService.parse(url) match {
      case Left(error) =>
        Future.successful(BadRequest(error))
      case Right((host, project, repo)) => getCommits(host, project, repo)
    }

  }

  
  
  
  
  /**
   * Url
   */
  @ApiOperation(
    value = "Get all tickets between two commits" //
    , httpMethod = "GET" //
    , response = classOf[Commit] //
    , responseContainer = "List" //
    )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Operation succeeded.") //
    , new ApiResponse(code = 404, message = "Did not find the resource.") //
    , new ApiResponse(code = 400, message = "Bad Request."), //
    new ApiResponse(code = 500, message = "Internal Server Error.") //
    ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "repo_url", value = "url of the repository", required = true, dataType = "string", paramType = "path") //
    ,new ApiImplicitParam(name = "from_commit_id", value = "from tickets committed after this commit-id", required = true, dataType = "string", paramType = "path") //
    ,new ApiImplicitParam(name = "to_commit_id", value = "to tickets commited untill this commit-id", required = true, dataType = "string", paramType = "path") //
    ))
  def ticketsByUrl(url: String, fromCommitId: String, toCommitId: String) = Action { NotImplemented }

  
  
  
  
  
  /**
   * Fetches commits from the specified repository, project and host.
   * @param host - hostname where the repository is hosted
   * @param project - project where the repository is grouped
   * @param repo - name of the repository
   * @return Action with the content(list of commits for the given repo)
   */
  @ApiOperation(
    notes = "On a github-server, a project is a username/organization. On stash-server a project is a project", //
    value = "Get all commits from the specified host, project and repository" //
    , httpMethod = "GET" //
    , response = classOf[Commit] //
    , responseContainer = "List" //
    )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Operation succeeded.") //
    , new ApiResponse(code = 301, message = "Moved permanently!") //
    , new ApiResponse(code = 404, message = "Did not find the resource.") //
    , new ApiResponse(code = 400, message = "Bad Request."), //
    new ApiResponse(code = 500, message = "Internal Server Error.") //
    ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "host", value = "hostname where the repository is hosted", required = true //
    , dataType = "string", paramType = "path") //
    , new ApiImplicitParam(name = "project", value = "project where the repository is grouped" //
    , required = true, dataType = "string", paramType = "path") //
    , new ApiImplicitParam(name = "repository", value = "name of the repository" //
    , required = true, dataType = "string", paramType = "path") //
    ))
  def commits(host: String, project: String, repo: String) = Action.async {
    getCommits(host, project, repo)
  }

  /**
   * Fetches commits from the specified repository, project and host.
   * @param host - hostname where the repository is hosted
   * @param project - project where the repository is grouped
   * @param repo - name of the repository
   * @return Future with the result[List of commits for the given repo]
   */
  private def getCommits(host: String, project: String, repo: String) = Future.firstCompletedOf(Seq(searchService.commits(host, project, repo))).map {
    case Left(error) =>
      logger.warn(error)
      InternalServerError(error)
    case Right(response) =>
      logger.info("Result: OK")
      Ok(Json.prettyPrint(Json.toJson(response))).as("application/json")
  }

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
    new ApiImplicitParam(name = "repo_url", value = "normalized url of the repository", required = true, dataType = "string", paramType = "path")))
  def normalize(repoUrl: String) = Action.async {

    val repository = UriEncoding.decodePath(repoUrl, "UTF-8")

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
          Future.successful(MovedPermanently(routes.RepoWS.get(URLEncoder.encode(normalizedUrl, "UTF-8")).url) //
            .withHeaders(NORMALIZED_REQUEST_PARAMETER -> normalizedUrl))
        }
    }
  }

  
    @ApiOperation(
     value = "Access repository's meta information" //
    , httpMethod = "HEAD" //
    )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "URI is correct and can be accessed if necessary.") //
    , new ApiResponse(code = 301, message = "Moved permanently!") //
    , new ApiResponse(code = 404, message = "Did not find the resource.") //
    , new ApiResponse(code = 400, message = "Bad request.") //
    , new ApiResponse(code = 500, message = "Internal Server Error.")))
  @ApiImplicitParams(Array( //
    new ApiImplicitParam(name = "repo_url", value = "normalized url of the repository", required = true, dataType = "string", paramType = "path")))
  def diffLink(url: String, fromCommitId: String, toCommitId: String) = Action { NotImplemented }

  def get(repo_url: String) = Action { NotImplemented }

}