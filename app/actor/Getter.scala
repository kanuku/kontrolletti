package actor

import scala.collection.JavaConverters.asScalaIteratorConverter
import Getter._
import akka.actor.Actor
import akka.actor.Status
import akka.actor.actorRef2Scala
import akka.pattern.pipe
import akka.actor.ActorLogging
import client.RequestDispatcher
import model.Commit
import javax.inject.Inject
import scala.concurrent.Future
import play.api.libs.ws.WSResponse
import client.scm.SCMResolver
import play.api.libs.ws.WSClient

object Getter {
  case class GetCommits(host: String, url: String, since: Option[String], pageNr: Int = 1, resolver: SCMResolver)
}

class Getter @Inject() (client: RequestDispatcher) extends Actor with ActorLogging {
  implicit val exec = context.dispatcher
  log.info("Initialized")

  def receive = {
    case GetCommits(host, url, since, pageNr, resolver) =>
      sender ! get(host, url, since, pageNr, resolver)
    case e: Status.Failure =>
      log.warning("Failed because of: " + e.cause)
  }

  def get(host: String, url: String, since: Option[String], pageNr: Int = 1, resolver: SCMResolver): Future[WSResponse] = {
    log.info(s"GET - host=$host - since=$since - pageNr=$pageNr - url=$url")

    if (resolver.isGithubServerType) {
      log.info(s"Putting the access-token in url($url)")
      client //
        .requestHolder(url) //
        .withQueryString(resolver.maximumPerPageQueryParameter()) //
        .withQueryString(resolver.startAtPageNumber(pageNr))
        .withQueryString(resolver.accessTokenHeader(host)).get()
    } else {
      log.info(s"Putting the access-token in head($url)")
      client //
        .requestHolder(url) //
        .withQueryString(resolver.maximumPerPageQueryParameter()) //
        .withQueryString(resolver.startAtPageNumber(pageNr))
        .withHeaders(resolver.authUserHeaderParameter(host))
        .withHeaders(resolver.accessTokenHeader(host))
        .withHeaders(resolver.proxyAuthorizationValue()).get()
    }
  }
}