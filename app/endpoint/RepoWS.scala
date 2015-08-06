package endpoint

import scala.concurrent.Future
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
import model.RepositoryResult

@Singleton
class RepoWS @Inject() (searchService: Search) extends Controller {

  import model.KontrollettiToModelParser._
  val logger: Logger = Logger { this.getClass }

  def normalize(repositoryUrl: String) = Action.async {
    val url = UriEncoding.decodePath(repositoryUrl, "UTF-8")
    logger.info(s"Request: $url")

    searchService.parse(url) match {
      case Left(error) =>
        logger.info("Result: 400:" + error)
        Future.successful(BadRequest)

      case Right((host, project, repo)) =>
        val normalizedUrl = searchService.normalize(host, project, repo)

        searchService.isRepo(host, project, repo).map {
          case Right(result) if (result && normalizedUrl.equals(url)) =>
            logger.info(s"Result: 200 $normalizedUrl")
            Ok
          case Right(result) if result =>
            logger.info(s"Result: 301 $normalizedUrl")
            MovedPermanently(routes.RepoWS.byUrl(URLEncoder.encode(normalizedUrl, "UTF-8")).url)
          case Left(error) =>
            logger.warn(s"Result: 500 $normalizedUrl")
            InternalServerError.as("application/problem+json")
          case Right(result) =>
            logger.info(s"Result: 404 $normalizedUrl")
            NotFound
        }

    }
  }

  def byUrl(repositoryUrl: String) = Action.async {
    val url = UriEncoding.decodePath(repositoryUrl, "UTF-8")
    logger.info(s"Request: $url")

    searchService.parse(url) match {
      case Right((host, project, repo)) =>
        searchService.repo(host, project, repo).map {
          case Right(None) =>
            logger.info(s"Result: 404 ")
            NotFound
          case Right(Some(result)) =>
            logger.info(s"Result: 200 ")
            Ok(Json.toJson(result)).as("application/x.zalando.repository+json")
          case Left(error) =>
            logger.info(s"Result: 500 ")
            InternalServerError.as("application/problem+json")
        }
      case Left(error) =>
        logger.info(s"Result: 400 $error")
        Future.successful(BadRequest(error).as("application/problem+json"))
    }

  }

}