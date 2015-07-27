package client

import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsError
import play.api.libs.json.JsPath
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import model.Author
import model.Commit
import model.Link
import model.Ticket
import play.api.Logger
import model.Repository

/**
 * Json deserializer for converting external json types, from the SCM,
 * into internal model.
 * Each SCM Server(Github or Stash) Should have a Parser per API Version
 */

sealed trait SCMParser {
  type Parser[A, B] = A => B
  private val logger: Logger = Logger(this.getClass())

  /**
   * Returns the list of domains that this parser can
   * parse json objects from.
   * @return list of domains
   */
  def domains: Set[String]

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

  /**
   * Returns the parser for deserializing a jsonValue to a List of Commits
   */
  def commitToModel: Parser[JsValue, Either[String, List[Commit]]]

  /**
   * Returns the parser for deserializing a jsonValue to a List of Tickets
   */
  def ticketToModel: Parser[JsValue, Either[String, List[Ticket]]]

  /**
   * Returns the parser for deserializing a jsonValue to a List of Authors
   */
  def authorToModel: Parser[JsValue, Either[String, List[Author]]]

  /**
   * Returns the parser for deserializing a jsonValue to a List of Repositories
   */
  def repoToModel: Parser[JsValue, Either[String, List[Repository]]]

  /**
   * Unwraps the result from the JsResult and returns the successfully deserialized
   * object or the detailed error message.
   * @return Either[Left,Right] - Left contains the error message and Right the deserialized Object
   */
  def extract[T](input: JsResult[T]): Either[String, T] = {
    input match {
      case s: JsSuccess[T] =>
        Right(s.get)
      case e: JsError =>
        logger.error("Failed to parse:" + e.errors)
        Left(s"Failed to parse!!")
    }
  }

}

/**
 * Deserializer for JsonObjects from Github.com
 *
 */
object GithubToJsonParser extends SCMParser {

  def domains = GithubResolver.hosts

  val commitToModel: Parser[JsValue, Either[String, List[Commit]]] = (value) => extract(value.validate[List[Commit]])
  val authorToModel: Parser[JsValue, Either[String, List[Author]]] = (author) => extract(author.validate[List[Author]])
  val ticketToModel: Parser[JsValue, Either[String, List[Ticket]]] = (value) => extract(value.validate[List[Ticket]])
  val repoToModel: Parser[JsValue, Either[String, List[Repository]]] = (value) => extract(value.validate[List[Repository]])

  private implicit val authorReader: Reads[Author] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "email").read[String] and
    Reads.pure(null) //
    )(Author.apply _)

  implicit val commitReader: Reads[Commit] = (
    (JsPath \ "sha").read[String] // id
    and (JsPath \ "commit" \ "message").read[String] // message
    //    and (JsPath  \ "sha").read[List[String]] //parentId
    and Reads.pure(null) // links
    and (JsPath \ "commit" \ "committer").read[Author]
    //    and Reads.pure(None) // valid
    and Reads.pure(null) // links
    )(Commit.apply _)

  implicit val ticketReader: Reads[Ticket] = null
  
  implicit val repoReader: Reads[Repository] = null
}

/**
 * Deserializer for JsonObjects from Stash
 *
 */
object StashToJsonParser extends SCMParser {

  def domains = StashResolver.hosts

  val commitToModel: Parser[JsValue, Either[String, List[Commit]]] = (value) => extract((value \ "values").validate[List[Commit]])
  val ticketToModel: Parser[JsValue, Either[String, List[Ticket]]] = (value) => extract(value.validate[List[Ticket]])
  val repoToModel: Parser[JsValue, Either[String, List[Repository]]] = (value) => extract(value.validate[List[Repository]])
  val authorToModel: Parser[JsValue, Either[String, List[Author]]] = (value) => extract(value.validate[List[Author]]) 

  private implicit val authorReader: Reads[Author] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "emailAddress").read[String] and
    Reads.pure(null) //
    )(Author.apply _)

  implicit val commitReader: Reads[Commit] = (
    (JsPath \ "id").read[String] //id
    and (JsPath \ "message").read[String] //message
    //    and (JsPath \ "parents" \ "id").read[List[String]] //parentId
    and Reads.pure(null) // parentId
    and (JsPath \ "author").read[Author] // author
    //    and Reads.pure(None) // valid
    and Reads.pure(null) // links
    )(Commit.apply _)
  implicit val ticketReader: Reads[Ticket] = null
  implicit val repoReader: Reads[Repository] = null
}