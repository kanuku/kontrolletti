package test.util

import module.Development
import play.api.db.DBApi
import play.api.inject.guice.GuiceApplicationBuilder
import org.scalatest.SuiteMixin
import org.scalatest.Suite
import play.api.Environment
import play.api.db.evolutions.Evolutions
import play.api.Configuration
import org.scalatest.BeforeAndAfterAll

/**
 * @author fbenjamin
 */
trait ApplicationWithDB extends SuiteMixin with BeforeAndAfterAll { this: Suite =>

  private def configuration() = Configuration.load(Environment.simple(), Map("config.resource" -> "application.test.db.conf"))

  val application = new GuiceApplicationBuilder() //
    .configure(configuration()) //
    .bindings(new Development())
    .build()

  def cleanupEvolutions = {
    val dbapi = application.injector.instanceOf[DBApi]
    val database = dbapi.database("default")
    Evolutions.cleanupEvolutions(database)
    database.shutdown()
    true
  }
  

  override def afterAll {
    cleanupEvolutions
  }
}