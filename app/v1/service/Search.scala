package v1.service

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import akka.dispatch.OnComplete
import akka.dispatch.OnFailure
import javax.inject._
import play.api.Logger
import play.api.libs.json._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.ws.WSResponse
import v1.client.GithubResolver
import v1.client.GithubToJsonParser
import v1.client.SCMClient
import v1.client.StashResolver
import v1.client.StashToJsonParser
import v1.model.Author
import v1.model.Commit
import v1.util.UrlParser
import com.typesafe.config.ConfigException.Parse
import play.api.libs.ws.EmptyBody

trait Search {
  def committers(url: String): Future[List[Author]]
  def commits(url: String): Future[List[Commit]]
}

/**
 * This class handles the calls to the right SCM (Stash/Github).
 *
 *
 */
@Singleton
class SearchImpl @Inject() (client: SCMClient) extends Search with UrlParser {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  import scala.concurrent._

  val github = GithubResolver
  val stash = StashResolver

  type ParsableUrl = (String) => (String, String, String)
  type CallableRequest = (String, String, String) => Future[WSResponse]
  type DeserializableResult[T] = (Future[WSResponse]) => List[T]

  val parseURL: ParsableUrl = { url => parse(url) }
  val callCommitters: CallableRequest = { (host, group, repo) => client.commits(host, group, repo) }

  /**
   *
   */
  def committers(url: String): Future[List[Author]] = {
    val parsedUrl = parseURL(url)

    handleWSRequest(url, f)
  }

  /**
   *
   */
  def commits(url: String): Future[List[Commit]] = {
    Logger.info(s"Searching commits for $url");
    val f: Callable = {}

    implicit val parser: Parser[Commit] = GithubToJsonParser.commitsGithubDeserializer
    handleWSRequest(url, f)
  }

  def handleWSRequest(url: String, f: Callable)(implicit parser: Parser[Commit]): Future[List[Commit]] = {
    parse(url) match {
      case ("", "", "") =>
        Future(Nil)
      case (host, group, repo) =>
        val res = call(host, group, repo, f)
        res.map {
          response =>
            deserialize(host, response)
        }
    }
  }

  def call(host: String, group: String, repo: String, f: Callable): Future[WSResponse] = {
    val res = f(host, group, repo)
    res onComplete {
      case Success(posts) =>
      case Failure(t) =>
        Logger.error("An error as occured: " + t.getMessage())
    }
    res.map { x => x }
  }

  def deserialize[T](host: String, response: WSResponse)(implicit parser: Parser[T]): List[T] = {
    if (github.names.contains(host)) {
      Logger.info(s"Parsing data from Github -> $host")
      import v1.client.GithubToJsonParser._

      parser(response)

    } else if (stash.names.contains(host)) {

      Logger.info(s"Parsing data from Stash -> $host")
      import v1.client.StashToJsonParser._
      parser(response)

    } else {
      Logger.warn(s"No parser found for -> $host")
      List()

    }

  }

}


