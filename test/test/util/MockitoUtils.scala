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

import client.SCM
import client.SCMImpl
import model.Commit
import model.Link
import model.Repository
import play.api.Application
import play.api.GlobalSettings
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.ws.WSRequestHolder
import play.api.libs.ws.WSResponse
import play.api.test.FakeApplication
import play.api.test.Helpers._
import service.Search
import service.SearchImpl

trait MockitoUtils extends MockitoSugar {

  /**
   * Creates a successfull/failed mocked WSResponse
   */
  def mockSuccessfullParsableFutureWSResponse[T](result: T, httpCode: Int): Future[WSResponse] = {
    val wsResponse = mock[WSResponse]
    val jsValue = mock[JsValue]
    val jsResult: JsResult[T] = new JsSuccess(result, null)

    when(jsValue.validate[T](anyObject())).thenReturn(jsResult)
    when(wsResponse.status).thenReturn(httpCode)
    when(wsResponse.json).thenReturn(jsValue)
    Future.successful(wsResponse)
  }

  /**
   *
   * Creates an implementation of the SCMCClient
   * where the method requestHolder is overridden to always return the passed `WSRequestHolder`,
   * which can be mocked.
   *
   */
  def createClient(requestParam: ((String) => WSRequestHolder)) = new SCMImpl {
    override def requestHolder = requestParam
  }

  def withFakeApplication(block: => Unit): Unit = {
    running(FakeApplication()) {
      block
    }
  }
  def withFakeApplication(global: GlobalSettings)(block: => Unit): Unit = {
    running(FakeApplication(withGlobal = Some(global))) {
      block
    }
  }

  class FakeGlobalWithSearchService(service: Search) extends play.api.GlobalSettings {
    private lazy val injector = Guice.createInjector(new AbstractModule {
      def configure() {
        bind(classOf[Search]).toInstance(service)
      }
    })
    override def getControllerInstance[A](clazz: Class[A]) = {
      injector.getInstance(clazz)
    }

  }

  class FakeGlobalWithFakeClient(client: SCM) extends play.api.GlobalSettings {
    private lazy val injector = Guice.createInjector(new AbstractModule {
      def configure() {
        bind(classOf[Search]).toInstance(new SearchImpl(client))
      }
    })
    override def getControllerInstance[A](clazz: Class[A]) = {
      injector.getInstance(clazz)
    }

  }

  def createRepository(href: String="href", project: String="project", host: String="host", repository: String="repo", commits: List[Commit]=List(), links: List[Link]=List()):Repository = {
   new Repository(href, project, host, repository, commits, links)
  }
}