

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json._
import org.joda.time.DateTime

/**
 * @author fbenjamin
 */
package object model {
  //Parsers
  val dateWrites = KontrollettiToJsonParser.dateWrites
  val dateReads = KontrollettiToModelParser.dateReads
  implicit val appInfoReads: Reads[AppInfo] = (
    (JsPath \ "scm_url").readNullable[String].map { _.getOrElse("") } and
    (JsPath \ "documentation_url").readNullable[String] and
    (JsPath \ "specification_url").readNullable[String] and
    (JsPath \ "last_modified").read[DateTime](dateReads))(AppInfo.apply _)

  implicit val appInfoWrites: Writes[AppInfo] = (
    (JsPath \ "scm_url").write[String] and
    (JsPath \ "documentation_url").writeNullable[String] and
    (JsPath \ "specification_url").writeNullable[String] and
    (JsPath \ "last_modified").write[DateTime](dateWrites))(unlift(AppInfo.unapply))

  implicit val appInfoFormat: Format[AppInfo] = Format(appInfoReads, appInfoWrites)

  implicit val errorFormat: Format[Error] = (
    (__ \ "detail").format[String] and
    (__ \ "status").format[Int] and
    (__ \ "errorType").format[String] //
    )(Error.apply, unlift(Error.unapply))

}

 