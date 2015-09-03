package controllers

import scala.io.Source._

import dao.DataStoreDAO
import javax.inject._
import model.AppInfo
import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
//scala.concurrent.ExecutionContext.Implicits.global.
@Singleton
class Main @Inject() (dataStore: DataStoreDAO) extends Controller {
  private val file = scala.io.Source.fromFile(Play.application.getFile("conf/swagger.json")).mkString
  val logger: Logger = Logger { this.getClass }

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def swagger = Action {
    logger.info(">>>> Started storing data in db<<<<<<")
    val input = List(new AppInfo("scm_url1", "doc_url1", "spec_url1", "last_modified1"))
    dataStore.saveApps(input)
    dataStore.appInfos().map { x =>
      logger.info("Future was executed.")
      logger.info(" result = >>>>>" + x.size)
    }

    Ok(views.html.swagger())
  }
  def status = Action {
    Ok
  }
  def specs = Action {
    Ok(file).as("application/json")
  }

}