package test.util

import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Environment
import play.api.Configuration

/**
 * @author fbenjamin
 */
trait TestApplicationLoader {
  private def configuration(applicationConf: String) = Configuration.load(Environment.simple(), Map("config.resource" -> applicationConf))
  def application(applicationConf: String ) = new GuiceApplicationBuilder().configure(configuration(applicationConf))

}