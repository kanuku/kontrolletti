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
case class Author(name: String, email:String)
case class Commit(id: String, message: String, author: Author)

trait JsonParserGithub {

  implicit val userWrites: Writes[Author] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "email").write[String])(unlift(Author.unapply))

  implicit val userReads: Reads[Author] = (
    (JsPath \ "name").read[String] and 
    (JsPath \ "email").read[String])(Author.apply _)

}





