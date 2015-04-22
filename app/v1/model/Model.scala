package v1.model

import play.api.libs.functional.syntax._
import play.api.libs.json._
/**
 * The model
 *
 */

case class Repository(name: String, resource: Resource, url: String, commits: List[Commit])
case class Commit(id: String, message: String, committer: User)
case class Resource(name: String, url: String)
case class User(login: String, id: Int, contributions: Int, url:String)

trait JsonParserGithub {

  implicit val userWrites: Writes[User] = (
    (JsPath \ "login").write[String] and
    (JsPath \ "id").write[Int] and
    (JsPath \ "contributions").write[Int] and
     (JsPath \ "url").write[String])(unlift(User.unapply))

  implicit val userReads: Reads[User] = (
    (JsPath \ "login").read[String] and
    (JsPath \ "id").read[Int] and
    (JsPath \ "contributions").read[Int] and
     (JsPath \ "url").read[String])(User.apply _)

}





