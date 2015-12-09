package utility

import scala.annotation.implicitNotFound
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.Logger
import java.sql.SQLException
import scala.util.Try
import scala.util.Failure
import scala.util.Success

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

  def handleError[T](f: Future[T])(implicit ec: ExecutionContext): Future[T] = {
    f recoverWith {

      case ex: SQLException =>
        logger.error(ex.getNextException.getMessage)
        logger.error(ex.getMessage)
        Future.failed(new Exception("Database operation failed!"))
    }
    f
  }

}