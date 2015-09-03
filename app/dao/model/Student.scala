package dao.model

import play.api.libs.json.Json

/**
 * Created by format on 15/8/20.
 */
case class Apps(id: Long, name: String, age: Int)

object Apps {

  implicit val studentFormat = Json.format[Apps]

}