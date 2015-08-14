package utility

import play.api.libs.json.Reads
import play.api.libs.json._
import model.Ticket

/**
 * @author fbenjamin
 */
trait TicketParser {

  val githubIssueNumberRegex = """"""
  val githubServerReferenceRegex = """"""
  val githubCommitMessageRegex = s""
  
//  implicit val readTicket:Reads[Ticket] = Reads[Ticket] { js =>
//    val l = (JsPath \ "parents" \\ "id")
//    val b: List[JsValue]= l (js)
//    Json.fromJson[Ticket](JsArray(b))
//  } 

  
  def parse(message:String):Ticket = ???
}

