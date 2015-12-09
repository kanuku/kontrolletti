package client.scm

import org.joda.time.DateTime
import configuration.SCMConfigurationImpl
import javax.inject.Inject
import model.{ Author, Commit }
import model.{ Repository, Ticket }
import play.api.Logger
import play.api.libs.functional.syntax.{ functionalCanBuildApplicative, toFunctionalBuilderOps }
import play.api.libs.json.{ JsPath, JsResult }
import play.api.libs.json.{ JsValue, Json, Reads }
import play.api.libs.json.JsArray
import utility.Transformer
import javax.inject.Named

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
    case host if domains.contains(host) => Option(this)
    case _                              => None
  }

  /**
   * Returns the parser for deserializing a jsonValue to a List of Commits
   */
  def commitToModel: Parser[JsValue, Either[String, List[Commit]]]
  /**
   * Returns the parser for deserializing a jsonValue to a single Commit
   */
  def singleCommitToModel: Parser[JsValue, Either[String, Commit]]

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
  def repoToModel: Parser[JsValue, Either[String, Repository]]

}

/**
 * Deserializer for JsonObjects from Github.com
 *
 */
class GithubToJsonParser @Inject() (@Named("github") resolver: SCMResolver) extends SCMParser {
  private val transformer = Transformer
  def domains = resolver.hosts.keySet
  val commitToModel: Parser[JsValue, Either[String, List[Commit]]] = (value) => transformer.deserialize2Either[List[Commit]](value)
  val singleCommitToModel: Parser[JsValue, Either[String, Commit]] = (value) => transformer.deserialize2Either[Commit](value)(commitReader)
  val authorToModel: Parser[JsValue, Either[String, List[Author]]] = (author) => transformer.deserialize2Either[List[Author]](author)
  val ticketToModel: Parser[JsValue, Either[String, List[Ticket]]] = (value) => transformer.deserialize2Either[List[Ticket]](value)
  val repoToModel: Parser[JsValue, Either[String, Repository]] = (value) => transformer.deserialize2Either[Repository](value)

  implicit val dateReads = Reads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ssZ")

  implicit val authorReader: Reads[Author] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "email").read[String] and
    Reads.pure(None) //
    )(Author.apply _)

  implicit val commitReader: Reads[Commit] = (
    (JsPath \ "sha").read[String] // id
    and (JsPath \ "commit" \ "message").read[String] // message
    and readUrls
    and (JsPath \ "commit" \ "committer").read[Author]
    and Reads.pure(None) // tickets
    and Reads.pure(None) // childId
    and Reads.pure(None) //0 links
    and (JsPath \ "commit" \ "author" \ "date").read[DateTime](dateReads)
    and Reads.pure("") //0 links
    )(Commit.apply _)

  def readUrls(implicit rt: Reads[String]) = Reads[Option[List[String]]] { js =>
    val pList: List[JsValue] = (JsPath \ "parents" \\ "sha")(js)
    Json.fromJson[List[String]](JsArray(pList)).map(Option(_))
  }

  implicit val ticketReader: Reads[Ticket] = (
    Reads.pure("")
    and Reads.pure("")
    and Reads.pure(None))(Ticket.apply _)

  implicit val repoReader: Reads[Repository] = (
    (JsPath \ "html_url").read[String]
    and Reads.pure("")
    and Reads.pure("")
    and Reads.pure("")
    and Reads.pure(true)
    and Reads.pure(None)
    and Reads.pure(None)
    and Reads.pure(None))(Repository.apply _)

}

/**
 * Deserializer for JsonObjects from Stash
 *
 */

class StashToJsonParser @Inject() (@Named("stash") resolver: SCMResolver) extends SCMParser {
  private val transformer = Transformer
  def domains = resolver.hosts.keys.toSet
  val commitToModel: Parser[JsValue, Either[String, List[Commit]]] = { value =>
    val res = (value \ "values")
    res.toOption match {
      case Some(jsValue) =>
        transformer.deserialize2Either[List[Commit]](jsValue)
      case None => Left("Failed to parse value")

    }
  }
  val singleCommitToModel: Parser[JsValue, Either[String, Commit]] = (value) => transformer.deserialize2Either[Commit](value)
  val ticketToModel: Parser[JsValue, Either[String, List[Ticket]]] = (value) => transformer.deserialize2Either[List[Ticket]](value)
  val repoToModel: Parser[JsValue, Either[String, Repository]] = (value) => transformer.deserialize2Either[Repository](value)
  val authorToModel: Parser[JsValue, Either[String, List[Author]]] = (value) => transformer.deserialize2Either[List[Author]](value)

  private implicit val authorReader: Reads[Author] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "emailAddress").read[String] and
    Reads.pure(None) //
    )(Author.apply _)

  //FIXME! Extract advanced reader (parent-ids) into its own function
  implicit val commitReader: Reads[Commit] = (
    (JsPath \ "id").read[String] //id
    and (JsPath \ "message").read[String] //message
    and readUrls
    and (JsPath \ "author").read[Author] // author
    and Reads.pure(None) // Tickets
    and Reads.pure(None) // valid
    and Reads.pure(None) //0 links
    and (JsPath \ "authorTimestamp").read[DateTime]
    and Reads.pure(""))(Commit.apply _)

  def readUrls(implicit rt: Reads[String]) = Reads[Option[List[String]]] { js =>
    val l = (JsPath \ "parents" \\ "id")
    val b: List[JsValue] = l(js)
    Json.fromJson[List[String]](JsArray(b)).map(Option(_))
  }
  implicit val ticketReader: Reads[Ticket] = (
    Reads.pure("")
    and Reads.pure("")
    and Reads.pure(None))(Ticket.apply _)

  implicit val repoReader: Reads[Repository] = (
    (JsPath \ "links" \ "self" \\ "href").read[String]
    and Reads.pure("")
    and Reads.pure("")
    and Reads.pure("")
    and Reads.pure(true)
    and Reads.pure(None)
    and Reads.pure(None)
    and Reads.pure(None))(Repository.apply _)

}
