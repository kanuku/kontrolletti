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
import model.AppInfo
import org.junit.internal.builders.AnnotatedBuilder


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

    when(jsValue.validate[String](anyObject())).thenReturn(jsResult)
    when(wsResponse.status).thenReturn(httpCode)
    when(wsResponse.json).thenReturn(jsValue)
    when(wsResponse.body).thenReturn(result)
    wsResponse
  }
  def withFakeApplication(block: => Unit): Unit = {
    running(FakeApplication()) {
      block
    }
  }
  def withFakeApplication(global: GlobalSettings)(block: => Unit): Unit = {
    running(FakeApplication(withGlobal = Option(global))) {
      block
    }
  }

  
  class FakeGlobalWithSearchService(service: Search) extends play.api.GlobalSettings {
    private lazy val injector = Guice.createInjector(new AbstractModule {
      def configure() {
        bind(classOf[Search]).toInstance(service)
        
      }
    })
    
  }

  class FakeGlobalWithFakeClient(client: SCM) extends play.api.GlobalSettings {
    lazy val injector = Guice.createInjector(new AbstractModule {
      def configure() {
        bind(classOf[Search]).toInstance(new SearchImpl(client))
      }
    })
    

  }
  
  def createCommitsResult(links: List[Link] = List(), commits: List[Commit] = List(createCommit())): CommitsResult = new CommitsResult(links, commits)

  def createRepository(href: String = "href", project: String = "project", host: String = "host", repository: String = "repo", commits: List[Commit] = List(), links: List[Link] = List()): Repository = new Repository(href, project, host, repository, Option(commits), Option(links))

  def createTicket(name: String = "name", description: String = "description", href: String = "href", links: List[Link] = List()) = new Ticket(name, href, links)

  def createCommit(id: String = "id", message: String = "message", parentId: List[String] = List(), author: Author = createAuthor(), valid: Option[Boolean] = None, links: List[Link] = List()): Commit = new Commit(id, message, parentId, author, None, None, Option(links))

  def createLink(href: String, method: String, rel: String, relType: String) = new Link(href, method, rel, relType)

  def createAuthor(name: String = "name", email: String = "email", links: List[Link] = List()): Author = new Author(name, email, Option(links))

  def createOAuthClientCredential(id: String, secret: String) = new OAuthClientCredential(id, secret)

  def createOAuthUserCredential(username: String, password: String) = new OAuthUserCredential(username, password)

  def createOAuthAccessToken(tokenType: String, accessToken: String, scope: String, expiresIn: Int) = new OAuthAccessToken(tokenType, accessToken, scope, expiresIn)

  def createAppInfo(scmUrl: String, specificationUrl: String, documentationUrl: String, serviceUrl: String, created: String, lastModified: String) = new AppInfo(scmUrl, serviceUrl, created, lastModified)
}