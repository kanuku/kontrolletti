package v1.model

import play.api.libs.functional.syntax._
import play.api.libs.json._


/**
 * The models
 *
 */

//case class Repository(name: String, resource: Resource, url: String, commits: List[Commit])
//case class Commit(id: String, message: String, committer: User)
//case class Resource(name: String, url: String)
case class Author(name: String, email: String)
case class Commit(id: String, message: String, valid: Boolean, author: Author)

trait JsonParserGithub {

  implicit val userWriter: Writes[Author] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "email").write[String])(unlift(Author.unapply))

  implicit val userReader: Reads[Author] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "email").read[String])(Author.apply _)

  implicit val commitWriter: Writes[Commit] = (
    (JsPath \ "id").write[String] and
    (JsPath \ "message").write[String] and
    (JsPath \ "valie").write[Boolean] and
    (JsPath \ "author").write[Author])(unlift(Commit.unapply))

  implicit val commitReader: Reads[Commit] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "message").read[String] and
    (JsPath \ "valid").read[Boolean] and
    (JsPath \ "author").read[Author])(Commit.apply _)

}





