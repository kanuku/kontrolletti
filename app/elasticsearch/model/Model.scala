package elasticsearch.model

/**
 * The model
 *
 */

case class Repository(url: String, commits: List[Commit])
case class Commit(id: String, message: String, author: Author)
case class Author(name: String, email: String)

/**
 * JsonModel trait provides an easy way of getting the implicit json-convertors for the model objects.
 * By inheriting from this trait, you get the json-convertors by default in scope.
 */
trait JsonModel {
  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  //Author
  implicit val authorWrites: Writes[Author] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "email").write[String])(unlift(Author.unapply))

  implicit val authorReads: Reads[Author] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "email").read[String])(Author.apply _)

  //Commit
  implicit val commitWrites: Writes[Commit] = (
    (JsPath \ "id").write[String] and
    (JsPath \ "message").write[String] and
    (JsPath \ "author").write[Author])(unlift(Commit.unapply))

  implicit val commitReads: Reads[Commit] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "message").read[String] and
    (JsPath \ "author").read[Author])(Commit.apply _)

  //Repository
  implicit val repositoryWrites: Writes[Repository] = (
    (JsPath \ "url").write[String] and
    (JsPath \ "commits").write[List[Commit]])(unlift(Repository.unapply))

  implicit val repositoryReads: Reads[Repository] = (
    (JsPath \ "url").read[String] and
    (JsPath \ "commits").read[List[Commit]])(Repository.apply _)

}








