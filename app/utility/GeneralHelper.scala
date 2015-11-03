package utility

import model.Ticket

trait GeneralHelper {

  def numberOfTickets(tickets: Option[List[Ticket]]): Int = tickets match {
    case None          => 0
    case Some(Nil)     => 0
    case Some(tickets) => tickets.size
  }
}