package client

import model.AppInfo
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json._
import model.KontrollettiToJsonParser
import model.KontrollettiToModelParser

package object cloudsearch {

  //Transformers for getting an id from a certain model
  type IdTransformer[T, String] = T => String
  implicit val app2Id: IdTransformer[AppInfo, String] = request => request.scmUrl

  def transform[T](input: List[T], operation: String)(implicit transform: IdTransformer[T, String]): List[UploadRequest[T]] =
    input.map { x => new UploadRequest(transform(x), operation, x) }.toList

  implicit def uploadRequestFormat[T: Format]: Format[UploadRequest[T]] =
    ((JsPath \ "id").format[String] and
      (JsPath \ "type").format[String] and
      (JsPath \ "fields").format[T])(UploadRequest.apply, unlift(UploadRequest.unapply))

  implicit def SearchResponse[T: Format]: Format[SearchResponse[T]] =
    (
      (JsPath \ "id").format[String] and
      (JsPath \ "type").format[String] and
      (JsPath \ "fields").format[T])(SearchResponse.apply, unlift(SearchResponse.unapply))

}