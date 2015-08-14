package utility

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import model.Ticket

/**
 * @author fbenjamin
 */
class TicketParserTest extends FlatSpec with Matchers {

  val ticketParser: TicketParser = new TicketParser {}

  //    "TicketParser#parse " 
  ignore must "parse ticket with " in {
    val message = "Committed ticket  that transforms messages to tickets #31"
    val result = new Ticket("#31", null, null)
    ticketParser.parse(message) eq result
  }

}