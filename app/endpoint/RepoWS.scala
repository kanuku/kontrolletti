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
import model.Repository
import model.KontrollettiToJsonParser._
import dao.RepoRepository
import scala.util.Try
import scala.util.Failure
import scala.util.Success

@Singleton
class RepoWS @Inject() (searchService: Search, repoRepository: RepoRepository) extends Controller {
  val logger: Logger = Logger { this.getClass }

  def normalize(repositoryUrl: String) = Action.async {
    val originalURL = UriEncoding.decodePath(repositoryUrl, "UTF-8").toLowerCase()
    logger.info(s"Request(Normalize): $originalURL")

    searchService.parse(originalURL) match {
      case Left(error) =>
        logger.info("Result: 400:" + error)
        Future.successful(BadRequest)
      case Right((host, project, repo)) =>
        Try(searchService.normalize(host, project, repo).toLowerCase()) match {
          case Failure(ex) =>
            logger.info(s"Result: 404 Unknown host")
            Future.successful(NotFound)
          case Success(normalizedUrl) =>
            logger.info(s"Normalized url $normalizedUrl")
            val encodedURL = URLEncoder.encode(normalizedUrl, "UTF-8")
            searchService.isRepo(host, project, repo).map {
              case Right(result) if (result && normalizedUrl.equals(originalURL)) =>
                logger.info(s"Result: 200 $normalizedUrl")
                Ok.withHeaders(X_NORMALIZED_REPOSITORY_URL_HEADER -> encodedURL)
              case Right(result) if result =>
                logger.info(s"Result: 301 $normalizedUrl")
                MovedPermanently(routes.RepoWS.byUrl(encodedURL).url).withHeaders(X_NORMALIZED_REPOSITORY_URL_HEADER -> encodedURL)
              case Left(error) =>
                logger.warn(s"Result: 500 $normalizedUrl")
                InternalServerError.as("application/problem+json")
              case Right(result) =>
                logger.info(s"Result: 404 $normalizedUrl")
                NotFound
            }
        }
    }
  }

  def byUrl(repositoryUrl: String) = Action.async {
    val url = UriEncoding.decodePath(repositoryUrl, "UTF-8")
    logger.info(s"Request(By URL): $url")

    searchService.parse(url) match {
      case Right((host, project, repo)) =>
        repoRepository.byParameters(host, project, repo) flatMap {
          case Some(result) =>
            logger.info(s"Result: 200 " + Json.toJson(result))
            Future.successful(Ok(Json.toJson(result)).as("application/x.zalando.repository+json"))
          case None =>
            logger.info(s"Not found in DB, fallback to check in SCM for repo: $url")
            searchService.isRepo(host, project, repo) flatMap {
              case Right(true) =>
                val result = Repository.fromUHPR(url, host, project, repo)
                logger.info(s"Result: 200 repo found in SCM ${Json.toJson(result)}")
                Future.successful(Ok(Json.toJson(result)).as("application/x.zalando.repository+json"))
              case _           =>
                logger.info(s"Result: 404 repo not found $url")
                Future.successful(NotFound)
            }
        }
      case Left(error) =>
        logger.info(s"Result: 400 $error")
        Future.successful(BadRequest(error).as("application/problem+json"))
    }

  }

}
