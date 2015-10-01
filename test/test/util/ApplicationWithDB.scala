package test.util

import com.google.inject.Guice

import module.Development
import play.api.Configuration
import play.api.Environment
import play.api.GlobalSettings
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.inject.guice.GuiceApplicationBuilder

/**
 * @author fbenjamin
 *
 * This Trait is ment for separating database tests from the production database during development/it-testing.
 * It loads its own separate
 */
trait ApplicationWithDB { //}extends OneAppPerSuite {

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

 
}