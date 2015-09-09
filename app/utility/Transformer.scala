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
case class LoadConfigurationException(message: String) extends Exception(message)
object Transformer {

  val logger: Logger = Logger { this.getClass }

  /**
   * Parse a String representing a json, and return it as a JsValue.
   *
   * @param input a String to parse
   * @return the JsValue representing the string in a future
   */
  def parse2Future(input: String): Future[JsValue] = {
    Try(Json.parse(input)) match {
      case Success(result) => Future.successful(result)
      case Failure(ex) =>
        logger.error("Error while parsing:" + ex.getMessage)
        Future.failed(new JsonParseException("Failed to parse the json input"))
    }
  }

  /**
   * Parses a String representing a json to a JsValue.
   *
   * @param input a String to parse
   * @return the Optional JsValue
   */
  def parse2Option(input: String): Option[JsValue] = Try(Json.parse(input)) match {
    case Success(result) =>
      Option(result)
    case Failure(ex) =>
      logger.error("Error while parsing:" + ex.getMessage)
      None
  }

  /**
   * Deserializes the input into the the Type T.
   * instance of the [T] type or the detailed error message.
   * @return Either[Left,Right] - Left contains the error message and Right the deserialized Object
   */
  def deserialize2Either[T](input: JsValue)(implicit rds: Reads[T]): Either[String, T] = {
    input.validate match {
      case s: JsSuccess[T] =>
        Right(s.get)
      case e: JsError =>
        logger.error("Failed to parse:" + e.errors)
        Left(s"Failed to parse!!")
    }
  }

  /**
   * Validates the input and returns the successfully transformed
   * instance of  [T] type.
   * @param input a JsValue
   * @return Option[T] - Contains the an optional deserialized Object
   */
  def deserialize2Option[T](input: JsValue)(implicit rds: Reads[T]): Option[T] = {
    input.validate match {
      case s: JsSuccess[T] =>
        Option(s.get)
      case e: JsError =>
        logger.error("Failed to parse:" + e.errors)
        None
    }
  }

  /**
   * Unwraps the result from the JsResult and returns the successfully deserialized
   * instance of the [T] wrapped in a complete future.
   *  @param input a JsValue
   * @return Future[T] - Future containing the desierialized object or the error message
   */
  def deserialize2Future[T](input: JsValue)(implicit rds: Reads[T]): Future[T] = input.validate match {
    case s: JsSuccess[T] => Future.successful(s.get)
    case e: JsError =>
      logger.error("Failed to parse:" + e.errors)
      Future.failed(new JsonParseException("Failed to parse the json-object"))
  }
  
  
  
}