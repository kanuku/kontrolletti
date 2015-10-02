package test.util

import module.Development
import play.api.Configuration
import play.api.Environment
import play.api.inject.Module
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.guice.GuiceInjectorBuilder
import play.api.inject.guice.GuiceableModule.fromGuiceModule
import play.api.inject.guice.GuiceableModule.fromPlayModule
import org.scalatest._
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._
import module.Development

/**
 * @author fbenjamin
 */
trait ApplicationWithCustomModule {

  def configuration() = Configuration.load(Environment.simple(), Map("config.resource" -> "application.test.conf") ++ customConfiguration)

  import play.api.inject.bind

  val application = new GuiceApplicationBuilder() //
    .configure(configuration()) //
    //    .bindings(new Development()) //
    .bindings(customModule()) //
    .build()

  def customModule():Module = new Module {
    def bindings(env: Environment, conf: Configuration) = Seq()
  }
  def customConfiguration: Map[String, String] = Map()

}