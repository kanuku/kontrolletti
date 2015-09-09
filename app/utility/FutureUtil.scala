package utility

import scala.annotation.implicitNotFound
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.Logger

/**
 * @author fbenjamin
 */
object FutureUtil {
  private val logger: Logger = Logger(this.getClass())
  def logErrorOnFailure[T](f: Future[T])(implicit ec: ExecutionContext): Future[T] = {
    logger.info("registering logErrorOnFailure on future")
    f onFailure {
      case t => logger.error("Future failed with: " + t.getMessage)

    }
    f
  }

}