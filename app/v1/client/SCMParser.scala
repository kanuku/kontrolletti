package v1.client

import play.api.Logger
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsError
import play.api.libs.json.JsPath
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import v1.model.Author
import v1.model.Commit
import org.scalactic._

/**
 * Json deserializer for converting external json types, from the SCM,
 * into internal model.
 */

sealed trait SCMParser {
  type Parser[A, B] = A => B
  /**
   * Returns the list of domains that this parser can
   * parse json objects from.
   * @return list of domains
   */
  def domains: List[String]

  /**
   * Resolves the Deserializer(parser) for a domain from a set of known parsers.
   * @param host domain of the scm server
   * @return the parser for the domain or None if none was found
   *
   */
  def resolve: PartialFunction[String, Option[SCMParser]] = {
    case host if domains.contains(host) => Some(this)
    case _                              => None
  }
  def commitToModel: Parser[JsValue, Either[String, List[Commit]]]
  def authorToModel: Parser[JsValue, Either[String, List[Author]]]
  def extract[T](input: JsResult[T]): Either[String, T] = {
    input match {
      case s: JsSuccess[T] =>
        Right(s.get)
      case e: JsError =>
        Left(s"Could not parse $input")
    }
  }

}

/**
 * Deserializer for objects from Github.com
 *
 */
object GithubToJsonParser extends SCMParser {

  def domains = GithubResolver.names

  val commitToModel: Parser[JsValue, Either[String, List[Commit]]] = {
    (value) => extract(value.validate[List[Commit]])
  }
  val authorToModel: Parser[JsValue, Either[String, List[Author]]] = {
    (author) => extract(author.validate[List[Author]])
  }

  private implicit val authorReader: Reads[Author] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "email").read[String])(Author.apply _)

  implicit val commitReader: Reads[Commit] = (
    (JsPath \ "sha").read[String]
    and (JsPath \ "commit" \ "message").read[String]
    and Reads.pure(None)
    and (JsPath \ "commit" \ "committer").read[Author])(Commit.apply _)
}

/**
 * Deserializer for objects from Stash SCM
 *
 */
object StashToJsonParser extends SCMParser {

  def domains = StashResolver.names

  val commitToModel: Parser[JsValue, Either[String, List[Commit]]] = {
    (value) => extract(value.validate[List[Commit]])
  }
  val authorToModel: Parser[JsValue, Either[String, List[Author]]] = {
    (author) => extract(author.validate[List[Author]])
  }

  private implicit val authorReader: Reads[Author] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "emailAddress").read[String])(Author.apply _)

  implicit val commitReader: Reads[Commit] = (
    (JsPath \ "id").read[String]
    and (JsPath \ "message").read[String]
    and Reads.pure(None)
    and (JsPath \ "author").read[Author])(Commit.apply _)

}