package endpoint

import com.wordnik.swagger.annotations.Api
import javax.inject._
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.utils.UriEncoding
import service.Search
import java.net.URLEncoder

@Api(value = "/api/hosts", description = "Committer information")
@Singleton
class CommitWS @Inject() (searchService: Search) extends Controller {

  import model.KontrollettiToModelParser._
  val logger: Logger = Logger { this.getClass }

  def diff(host: String, project: String, repository: String, sourceId: String, targetId: String) = Action.async {
    searchService.diffExists(host, project, repository, sourceId, targetId).map {
      case Left(error) =>
        logger.info("Result 500: " + error)
        InternalServerError.as("application/problem+json")
      case Right(None) =>
        logger.info("Result 404")
        NotFound
      case Right(Some(link)) =>
        logger.info("Result 301: " + link.href)
        Redirect(URLEncoder.encode(link.href, "UTF-8"))
    }
  }
}

