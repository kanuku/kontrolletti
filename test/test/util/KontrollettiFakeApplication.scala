package test.util

import play.api.Configuration
import play.api.Environment
import play.api.inject.guice.GuiceApplicationBuilder

/**
 * @author fbenjamin
 */
trait KontrollettiFakeApplication {
  private val configuration = Configuration.load(Environment.simple(), Map("config.resource" -> "application.test.conf"))
    val application = new GuiceApplicationBuilder().configure(configuration)

  def withTestDatabaseConfigured(block: Unit) {
    block
  }

}