package endpoint

import javax.inject.Inject
import javax.inject.Singleton
import play.api.Logger
import play.api.Play
import play.api.Play.current
import play.api.mvc.Action
import play.api.mvc.Controller
import scala.concurrent.ExecutionContext
import akka.actor.ActorRef
import javax.inject.Named
import actor.Job._

@Singleton
class Main @Inject()(
 @Named("job-actor") configuredActor: ActorRef    
) extends Controller {
  private val file = scala.io.Source.fromFile(Play.application.getFile("conf/swagger.json")).mkString
  val logger: Logger = Logger { this.getClass }

  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def swagger = Action {
    Ok(views.html.swagger())
  }
  def status = Action {
    logger.info("status")
    configuredActor ! IMPORT_REPOSITORIES_KIO
    Ok
  }
  def specs = Action {
    Ok(file).as("application/json")
  }

}