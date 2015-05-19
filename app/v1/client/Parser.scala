package v1.client

import play.api.libs.functional.syntax._
import play.api.libs.json._
import v1.model.Author
import v1.model.Commit
import play.api.libs.ws.WSResponse

object GithubToJsonParser {
  def domains = GithubResolver.names

  implicit val authorGithubDeserializer: (WSResponse) => List[Author] = (response) =>
    response.json.validate[List[Author]].get

  implicit val authorReader: Reads[Author] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "emailAddress").read[String])(Author.apply _)

  implicit val commitsGithubDeserializer: (WSResponse) => List[Commit] = (response) =>
    response.json.validate[List[Commit]].get

  implicit val commitReader: Reads[Commit] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "message").read[String] and
    (JsPath \ "valid").read[Boolean] and
    (JsPath \ "author").read[Author])(Commit.apply _)
}

object StashToJsonParser {

  def domains = StashResolver.names

  implicit val authorStashDeserializer: (WSResponse) => List[Author] = (response) =>
    response.json.validate[List[Author]].get

  implicit val commitsStashDeserializer: (WSResponse) => List[Commit] = (response) =>
    response.json.validate[List[Commit]].get

  implicit val commitReader: Reads[Commit] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "message").read[String] and
    (JsPath \ "valid").read[Boolean] and
    (JsPath \ "author").read[Author])(Commit.apply _)

  implicit val authorReader: Reads[Author] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "emailAddress").read[String])(Author.apply _)
}

object KontrollettiToJsonParser {
  implicit val authorWriter: Writes[Author] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "emailAddress").write[String])(unlift(Author.unapply))

  implicit val commitWriter: Writes[Commit] = (
    (JsPath \ "id").write[String] and
    (JsPath \ "message").write[String] and
    (JsPath \ "valid").write[Boolean] and
    (JsPath \ "author").write[Author])(unlift(Commit.unapply))

}