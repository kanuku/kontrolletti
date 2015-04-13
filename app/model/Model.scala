package model

/**
 * The model
 *
 */

case class Repository(url: String, commits: List[Commit])
case class Commit(id: String, message: String, committer: Committer)
case class Committer(name: String, email: String)

/**
 * JsonModel trait provides an easy way of getting the implicit json-convertors for the model objects.
 * By inheriting from this trait, you get the json-convertors by default in scope.
 */
trait JsonModel {
  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  //Committer
  implicit val committerWrites: Writes[Committer] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "email").write[String])(unlift(Committer.unapply))

  implicit val committerReads: Reads[Committer] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "email").read[String])(Committer.apply _)

  //Commit
  implicit val commitWrites: Writes[Commit] = (
    (JsPath \ "id").write[String] and
    (JsPath \ "message").write[String] and
    (JsPath \ "committer").write[Committer])(unlift(Commit.unapply))

  implicit val commitReads: Reads[Commit] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "message").read[String] and
    (JsPath \ "committer").read[Committer])(Commit.apply _)

  //Repository
  implicit val repositoryWrites: Writes[Repository] = (
    (JsPath \ "url").write[String] and
    (JsPath \ "commits").write[List[Commit]])(unlift(Repository.unapply))

  implicit val repositoryReads: Reads[Repository] = (
    (JsPath \ "url").read[String] and
    (JsPath \ "commits").read[List[Commit]])(Repository.apply _)

}








