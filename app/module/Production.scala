package module

import com.google.inject.AbstractModule
import service.SearchImpl
import service.Search
import client.SCM
import client.SCMImpl
import play.api.Logger

class Production extends AbstractModule {
  private val logger: Logger = Logger(this.getClass())
  def configure() {
    logger.info("Configured with a production module")
    bind(classOf[Search]).to(classOf[SearchImpl])
    bind(classOf[SCM]).to(classOf[SCMImpl])

  }
}