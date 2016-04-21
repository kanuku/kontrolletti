package utility

import akka.actor.ActorSystem
import java.sql.SQLException
import play.api.Logger
import scala.annotation.implicitNotFound
import scala.util.Try
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration.FiniteDuration

/**
 * @author fbenjamin
 */
object FutureUtil {
  private val logger: Logger = Logger(this.getClass())

  def logErrorOnFailure[T](f: Future[T])(implicit ec: ExecutionContext): Future[T] = {
    f onFailure {
      case t => logger.error("Future failed with: " + t.getMessage)

    }
    f
  }

  // TODO: remove tryFuture and its usage
  /**
   * Handles a future in a Try and returns the future if it doesn't fail. Otherwise it will log the error and return the default value.
   */
  def tryFuture[A](call: => A, default: A): A = try {
    call
  } catch {
    case e: Throwable =>
      logger.error(e.getMessage)
      default
  }
  /**
   * Handles a future in a Try and returns the future if it doesn't fail. Otherwise it will log the error and return the default value.
   */
  def tryFuture[A](call: => A): Option[A] = try {
    Option(call)
  } catch {
    case e: Throwable =>
      logger.error(e.getMessage)
      None
  }

  def handleError[T](f: Future[T])(implicit ec: ExecutionContext): Future[T] =
    f recoverWith {

      case ex: SQLException =>
        //logger.error(ex.getNextException.getMessage)
        //logger.error(ex.getMessage)
        logger.error("db exception", ex)
        Try(logger.error("reason: ", ex.getNextException))
        Future.failed(ex)
    }

  def timeoutFuture(actorSys: ActorSystem, du: FiniteDuration)(implicit ec: ExecutionContext): Future[Unit] = {
    val p = Promise[Unit]
    actorSys.scheduler.scheduleOnce(du) {
      p.trySuccess(())
      ()
    }
    p.future
  }

}
