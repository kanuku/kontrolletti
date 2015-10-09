package test.util

import scala.concurrent.Future
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import com.google.inject.AbstractModule
import com.google.inject.Guice
import client.scm.SCM
import client.scm.SCMImpl
import model.Commit
import model.Link
import model.Repository
import play.api.Application
import play.api.GlobalSettings
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.ws.WSResponse
import org.scalatest._
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._
import service.Search
import service.SearchImpl
import model.Ticket
import model.Author
import model.Author
import model.CommitsResult
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsPath
import client.oauth.OAuthClientCredential
import client.oauth.OAuthUserCredential
import client.oauth.OAuthAccessToken
import org.junit.internal.builders.AnnotatedBuilder
import org.joda.time.DateTime
import play.api.libs.json.Reads

trait MockitoUtils extends MockitoSugar {

  /**
   * Creates a mocked WSResponse
   */
  def mockSuccessfullParsableFutureWSResponse[T](result: String, httpCode: Int): Future[WSResponse] = {
    Future.successful {
      createMockedWSResponse(result, httpCode)
    }
  }
  def createMockedWSResponse[T](result: String, httpCode: Int): WSResponse = {
    val wsResponse = mock[WSResponse]
    val jsValue = mock[JsValue]
    val jsResult: JsResult[String] = new JsSuccess(result)

    when(jsValue.validate[String](any[Reads[String]]())).thenReturn(jsResult)
    when(wsResponse.status).thenReturn(httpCode)
    when(wsResponse.json).thenReturn(jsValue)
    when(wsResponse.body).thenReturn(result)
    wsResponse
  }

  def createCommitsResult(links: List[Link] = List(), commits: List[Commit] = List(createCommit())): CommitsResult = new CommitsResult(links, commits)

  def createRepository(url: String = "url", host: String = "host", project: String = "project", repository: String = "repository", enabled: Boolean = true, lastSync: Option[DateTime] = None, lastFailed: Option[DateTime] = None, links: Option[List[Link]] = None): Repository = new Repository(url, host, project, repository, enabled, lastSync, lastFailed, links)

  def createTicket(name: String = "name", description: String = "description", href: String = "href", links: List[Link] = List()) = new Ticket(name, href, Option(links))

  def createCommit(id: String = "id", message: String = "message", parentIds: Option[List[String]] = None, author: Author = createAuthor(), childId: Option[String] = None, tickets: Option[List[Ticket]] = None, valid: Option[Boolean] = None, links: Option[List[Link]] = None, date: DateTime = new DateTime, repoUrl: String = "repoUrl"): Commit = new Commit(id, message, parentIds, author, childId, tickets, valid, links, date, repoUrl)

  def createLink(href: String, method: String, rel: String, relType: String) = new Link(href, method, rel, relType)

  def createAuthor(name: String = "name", email: String = "email", links: Option[List[Link]] = None): Author = new Author(name, email, links)

  def createOAuthClientCredential(id: String, secret: String) = new OAuthClientCredential(id, secret)

  def createOAuthUserCredential(username: String, password: String) = new OAuthUserCredential(username, password)

  def createOAuthAccessToken(tokenType: String, accessToken: String, scope: String, expiresIn: Int) = new OAuthAccessToken(tokenType, accessToken, scope, expiresIn)

}