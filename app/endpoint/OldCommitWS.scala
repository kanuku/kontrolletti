package endpoint

import javax.inject._
import model._
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Result
import service.Search
import scala.concurrent.Future


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
  private def getCommits(host: String, project: String, repo: String) = {
    Future.successful(NotImplemented)
  } 
    
//    Future.firstCompletedOf(Seq(searchService.commits(host, project, repo))).map {
//    case Left(error) =>
//      logger.warn(error)
//      InternalServerError(error)
//    case Right(response) if (response == null || response.isEmpty) =>
//      logger.info("Result: NotFound(404)")
//      asJsonCommmitType(NotFound)
//    case Right(response) =>
//      logger.info("Result: OK")
//      asJsonCommmitType(Ok(Json.prettyPrint(Json.toJson(response))))
//  }

  private def asJsonCommmitType(result: Result) = result.as("application/x.zalando.commit+json")
}