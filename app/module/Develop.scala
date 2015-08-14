package module

import com.google.inject.AbstractModule
import client.scm.SCM
import client.scm.SCMImpl
import service.Search
import service.SearchImpl
import play.api.Logger
import client.RequestDispatcherImpl
import client.RequestDispatcher

class Develop extends AbstractModule {
  private val logger: Logger = Logger(this.getClass())
  def configure() {
    logger.info("Configured with a develop module")
    bind(classOf[Search]).to(classOf[SearchImpl])
    bind(classOf[SCM]).to(classOf[SCMImpl])
    bind(classOf[RequestDispatcher]).to(classOf[RequestDispatcherImpl])
  }
}