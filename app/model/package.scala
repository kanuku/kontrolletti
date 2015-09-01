

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json._

/**
 * @author fbenjamin
 */
package object model {
  //Parsers
  implicit val appInfoFormat: Format[AppInfo] = (
    (JsPath \ "scm_url").format[String] and
    (JsPath \ "documentation_url").format[String] and
    (JsPath \ "specification_url").format[String] and
    (JsPath \ "last_modified").format[String])(AppInfo.apply, unlift(AppInfo.unapply))

    
     implicit val errorFormat: Format[Error] = (
    (__ \ "detail").format[String] and
    (__ \ "status").format[Int] and
    (__ \ "errorType").format[String] //
    )(Error.apply, unlift(Error.unapply)) 

}

 