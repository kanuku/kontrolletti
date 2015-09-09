

/**
 * @author fbenjamin
 */
import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import utility.Transformer
import play.api.Logger
import play.api.libs.json.Writes
package object dao {
  private val logger: Logger = Logger(this.getClass())
  abstract class DefaultDeserializer[T](jsonValue: JsValue)(implicit rds: Reads[T]) {
    def deserialize: Option[T] = {
      Transformer.deserialize2Option(jsonValue)
    }
  }
  case class TableTest(id:String,value:JsValue)
  case class TableDefinitionKey1[T](id1: String, jsonValue: JsValue)(implicit rds: Reads[T]) extends DefaultDeserializer[T](jsonValue)
  case class TableDefinitionKey2[T](id1: String, id2: String, jsonValue: JsValue)(implicit rds: Reads[T]) extends DefaultDeserializer[T](jsonValue)

  
  
}