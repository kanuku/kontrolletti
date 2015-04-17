package v1.model

/**
 * The model
 *
 */

case class Repository(name: String, resource: Resource, url: String, commits: List[Commit])
case class Commit(id: String, message: String, committer: User)
case class Resource(name: String, url: String)
case class User(name: String, email: String)

/**
 * JsonModel trait provides an easy way of getting the implicit json-convertors for the model objects.
 * By inheriting from this trait, you get the json-convertors by default in scope.
 */
trait JsonModel {
  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  //Committer
  implicit val committerWrites: Writes[User] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "email").write[String])(unlift(User.unapply))

  implicit val committerReads: Reads[User] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "email").read[String])(User.apply _)

  //Commit
  implicit val commitWrites: Writes[Commit] = (
    (JsPath \ "id").write[String] and
    (JsPath \ "message").write[String] and
    (JsPath \ "committer").write[User])(unlift(Commit.unapply))

  implicit val commitReads: Reads[Commit] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "message").read[String] and
    (JsPath \ "committer").read[User])(Commit.apply _)

  //Repository
  implicit val repositoryWrites: Writes[Repository] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "resource").write[Resource] and 
    (JsPath \ "url").write[String] and
    (JsPath \ "commits").write[List[Commit]])(unlift(Repository.unapply))

  implicit val repositoryReads: Reads[Repository] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "resource").read[Resource] and
    (JsPath \ "url").read[String] and
    (JsPath \ "commits").read[List[Commit]])(Repository.apply _)

  // resource
  implicit val resourceWrites: Writes[Resource] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "url").write[String])(unlift(Resource.unapply))

  implicit val resourceReads: Reads[Resource] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "url").read[String])(Resource.apply _)

}








