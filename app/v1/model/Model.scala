package v1.model

import play.api.libs.functional.syntax._
import play.api.libs.json._
/**
 * The models
 *
 */

//case class Repository(name: String, resource: Resource, url: String, commits: List[Commit])
//case class Resource(name: String, url: String)
case class User(id: Int, login: String, email: String)
case class Commit(id: String, author: User, message: String, committer: User)

trait JsonParserGithub {

  implicit val userWrites: Writes[User] = (
    (JsPath \ "id").write[Int] and
    (JsPath \ "login").write[String] and
    (JsPath \ "email").write[String])(unlift(User.unapply))

  implicit val userReads: Reads[User] = (
    (JsPath \ "id").read[Int] and
    (JsPath \ "login").read[String] and
    (JsPath \ "email").read[String])(User.apply _)

}





