package model

import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.JsPath
import play.api.libs.json.Reads
import play.api.libs.json.Writes

/**
 * The models
 *
 */


case class Author(name: String, email: String)
case class Commit(id: String, message: String, valid: Option[Boolean], author: Author)

//TODO: Evaluate Moving the readers in this parser(KontrollettiToJsonParser) into Companion objects
// And overriding those companion objects in the SCM Parser

object KontrollettiToJsonParser {
  implicit val authorReader: Reads[Author] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "email").read[String])(Author.apply _)

  implicit val commitReader: Reads[Commit] = (
    (JsPath \ "id").read[String]
    and (JsPath \ "message").read[String]
    and Reads.pure(None)
    and (JsPath \ "author").read[Author])(Commit.apply _)

  implicit val authorWriter: Writes[Author] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "email").write[String])(unlift(Author.unapply))

  implicit val commitWriter: Writes[Commit] = (
    (JsPath \ "id").write[String]
    and (JsPath \ "message").write[String]
    and (JsPath \ "valid").writeNullable[Boolean]
    and (JsPath \ "author").write[Author])(unlift(Commit.unapply))
}



