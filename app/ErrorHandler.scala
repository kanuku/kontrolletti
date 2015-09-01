

import play.api.http.HttpErrorHandler
import scala.concurrent.Future
import play.api.mvc.RequestHeader
import play.api.mvc.Results.InternalServerError
import play.api.mvc.Results.NotFound
import play.api.mvc.Results.Status
import play.api.Logger

/**
 * @author fbenjamin
 */
class ErrorHandler extends HttpErrorHandler {
  private val logger: Logger = Logger(this.getClass())
  // 500 - internal server error
  def onServerError(request: RequestHeader, throwable: Throwable) = {
    logger.error("A Server error occurred" + throwable.getMessage)
    Future.successful(InternalServerError(views.html.errors.onError(throwable)))
  }

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    logger.error(s"An client error occurred: status($statusCode) message=$message")
    if (statusCode == play.api.http.Status.NOT_FOUND)
      Future.successful(NotFound(views.html.errors.onHandlerNotFound(request)))
    else
      Future.successful(Status(statusCode)("A client error occurred: " + message))
  }

}