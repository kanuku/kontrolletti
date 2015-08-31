package client

import model.AppInfo
import play.api.libs.functional.syntax._
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json._
import play.api.libs.json._
import utility.JsonParseException
import scala.concurrent.Future
import model.Commit
import model.Ticket

package object cloudsearch {

  //Transformers for generating an id from a certain model
  type IdTransformer[T, String] = T => String
  implicit val app2Id: IdTransformer[AppInfo, String] = input => input.scmUrl
  
  implicit val ticket2Id: IdTransformer[Ticket, String] = input => input.href
  
  //Can't be implicit because app is necessary
  def commit2Id(app: AppInfo): IdTransformer[Commit, String] = input => app.scmUrl + "-" + input.id

  def transform2FutureUploadRequest[T](input: List[T], operation: String)(implicit transformer: IdTransformer[T, String]): Future[List[UploadRequest[T]]] =
    Future.successful(transform2UploadRequest[T](input, operation))

  def transform2UploadRequest[T](input: List[T], operation: String)(implicit transform: IdTransformer[T, String]): List[UploadRequest[T]] =
    input.map { x => new UploadRequest(transform(x), operation, x) }.toList

  implicit def uploadRequestFormat[T: Format]: Format[UploadRequest[T]] =
    ((JsPath \ "id").format[String] and
      (JsPath \ "type").format[String] and
      (JsPath \ "fields").format[T])(UploadRequest.apply, unlift(UploadRequest.unapply))

  implicit def appInfoResponseRead: Reads[AppInfoResponse] = (
    (JsPath \ "hits" \ "found").read[Int] and
    (JsPath \ "hits" \ "start").read[Int] and
    readOptionalList[AppInfo]((JsPath \ "hits" \ "hit" \\ "fields")))(AppInfoResponse.apply _)

  def readOptionalList[T](path: JsPath)(implicit rt: Reads[T]) = Reads[Option[List[T]]] { json =>
    Json.fromJson[Option[List[T]]](JsArray(path(json)))
  }

  def formatString[String: Format]: Format[List[String]] = (
    (JsPath \ "hits" \ "hit" \\ "id").format[List[String]])

}