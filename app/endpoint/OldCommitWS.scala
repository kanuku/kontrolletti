package endpoint

import scala.concurrent.Future
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.annotations.ApiImplicitParam
import com.wordnik.swagger.annotations.ApiImplicitParams
import com.wordnik.swagger.annotations.ApiOperation
import com.wordnik.swagger.annotations.ApiResponse
import com.wordnik.swagger.annotations.ApiResponses
import javax.inject._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action
import play.api.mvc.Controller
import model._
import play.api.Logger
import service.Search
import play.api.libs.json.Json
import play.api.mvc.Result

@Api(value = "/api/hosts", description = "Committer information")
@Singleton
class OldCommitWS @Inject() (searchService: Search) extends Controller {

  import model.KontrollettiToModelParser._
  val logger: Logger = Logger { this.getClass }

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

  def byId(host: String, project: String, repository: String, id: String) = Action { NotImplemented }
  
  
  
  def diff(host: String, project: String, repository: String, sourceId: String, targetId: String) = Action { NotImplemented }

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
    case Right(response) if (response == null || response.isEmpty) =>
      logger.info("Result: NotFound(404)")
      asJsonCommmitType(NotFound)
    case Right(response) =>
      logger.info("Result: OK")
      asJsonCommmitType(Ok(Json.prettyPrint(Json.toJson(response))))
  }

  private def asJsonCommmitType(result: Result) = result.as("application/x.zalando.commit+json")
}