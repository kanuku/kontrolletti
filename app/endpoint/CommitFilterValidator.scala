package endpoint

import model.Error
import dao.FilterParameters
import org.joda.time.DateTime
import dao.FilterParameters
import dao.FilterParameters
import scala.Left
import scala.Right
import org.joda.time.format.ISODateTimeFormat
import play.api.mvc.Results.Status
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import play.api.Logger
trait CommitFilterValidator {
  private val logger: Logger = Logger { this.getClass }
  val combinedNotAllowedError = "since and until parameters may not be combined with from_Date & to_Date"
  val dateTimeParsingError = "One of the date-time(ISO8601) parameters could not be parsed!"
  def validate(sinceId: Option[String] = None, //
               untilId: Option[String] = None, //
               isValid: Option[Boolean] = None, //
               sinceDate: Option[String] = None, //
               untilDate: Option[String] = None //
               )(implicit status: Int, errorType: String): Either[Error, FilterParameters] = (sinceId, untilId, sinceDate, untilDate) match {
    case (None, None, None, None) =>
      Right(FilterParameters(None, None, isValid, None, None))
    //Both commitId's and Date's should not be acceptable
    case (since, until, fromDate, toDate) if ((since.isDefined | until.isDefined) && (fromDate.isDefined || toDate.isDefined)) =>
      Left(Error(combinedNotAllowedError, status, errorType))
    case (since, until, None, None) =>
      Right(FilterParameters(since, until, isValid, None, None))
    case (None, None, fromDate, toDate) =>
      (parseDateTime(fromDate), parseDateTime(toDate)) match {
        case (Right(fromDate), Right(toDate)) =>
          Right(FilterParameters(None, None, isValid, fromDate, toDate))
        case _ => Left(Error(dateTimeParsingError, status, errorType))
      }
  }

  private def parseDateTime(date: Option[String]): Either[String, Option[DateTime]] = Try(date.map { x => ISODateTimeFormat.dateTimeParser().parseDateTime(x) }) match {
    case Success(s) => Right(s)
    case Failure(e) =>
      val msg = s"Failed to parse the date $date"
      logger.error(msg+ e.getMessage)
      Left(msg)
  }
}