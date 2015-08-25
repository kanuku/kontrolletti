package client.cloudsearch

import model.AppInfo
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json._
import model.KontrollettiToJsonParser
import model.KontrollettiToModelParser

package object utils {

  //Transformers for getting an id from a certain model
  type IdTransformer[T, String] = T => String
  implicit val app2Id: IdTransformer[AppInfo, String] = request => request.scmUrl

  def transform[T](input: List[T], operation: String)(implicit transform: IdTransformer[T, String]): List[UploadDocument[T]] =
    input.map { x => new UploadDocument(transform(x), operation, x) }.toList

  //Parsers
  implicit val appInfoFormat: Format[AppInfo] = Format(KontrollettiToModelParser.appInfoReader, KontrollettiToJsonParser.appInfoWriter)

  implicit def uploadDocumentWrites[T: Format]: Format[UploadDocument[T]] =
    ((JsPath \ "id").format[String] and
      (JsPath \ "type").format[String] and
      (JsPath \ "fields").format[T])(UploadDocument.apply, unlift(UploadDocument.unapply))

}