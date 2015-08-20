package utility

import scala.Left
import scala.Right
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Logger
import play.api.libs.json.JsError
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Reads

/**
 * @author fbenjamin
 */

case class JsonParseException(message: String) extends Exception(message)

object Transformer {

  val logger: Logger = Logger { this.getClass }

  /**
   * Parse a String representing a json, and return it as a JsValue.
   *
   * @param input a String to parse
   * @return the JsValue representing the string in a future
   */
  def parse(input: String): Future[JsValue] = {
    Try(Json.parse(input)) match {
      case Success(result) => Future.successful(result)
      case Failure(ex) =>
        logger.error("Error while parsing:" + ex.getMessage)
        Future.failed(new JsonParseException("Failed to parse the json input"))
    }
  }

  /**
   * Transforms a String into a Object of type T
   *
   * @param input a String to parse
   * @return the JsValue representing the string in a future
   */
  def transform[T](input: String)(implicit rds: Reads[T]): Future[T] = parse(input).flatMap { jsvalue =>
    extract2Future(jsvalue.validate)
  }

  /**
   * Unwraps the result from the JsResult and returns the successfully deserialized
   * instance of the [T] type or the detailed error message.
   * @return Either[Left,Right] - Left contains the error message and Right the deserialized Object
   */
  def extract2Either[T](input: JsResult[T]): Either[String, T] = {
    input match {
      case s: JsSuccess[T] =>
        Right(s.get)
      case e: JsError =>
        logger.error("Failed to parse:" + e.errors)
        Left(s"Failed to parse!!")
    }
  }
  /**
   * Unwraps the result from the JsResult and returns the successfully deserialized
   * instance of the [T] type or the detailed error message.
   * @return Either[Left,Right] - Left contains the error message and Right the deserialized Object
   */
  def extract2Future[T](input: JsResult[T]): Future[T] = {
    input match {
      case s: JsSuccess[T] => Future.successful(s.get)
      case e: JsError =>
        logger.error("Failed to parse:" + e.errors)
        Future.failed(new JsonParseException("Failed to parse the json-object"))
    }
  }
}