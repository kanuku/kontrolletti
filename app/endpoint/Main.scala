package endpoint

import javax.inject.Inject
import javax.inject.Singleton
import play.api.Logger
import play.api.Play
import play.api.Play.current
import play.api.mvc.Action
import play.api.mvc.Controller
import dao.AppsRepository
import scala.concurrent.ExecutionContext
@Singleton
class Main @Inject() (repo:AppsRepository)  (implicit ex: ExecutionContext)  extends Controller {
  private val file = scala.io.Source.fromFile(Play.application.getFile("conf/swagger.json")).mkString
  val logger: Logger = Logger { this.getClass }

  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def swagger = Action {
    logger.info("WHAZAAAAP")
     repo.list().map { student =>
      logger.error(">>>>"+student.size)
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