package utility

import org.scalatest.{ BeforeAndAfter, FlatSpec }
import org.scalatest.mock.MockitoSugar
import org.scalatest.Matchers
import test.util.MockitoUtils

class GeneralHelperTest extends FlatSpec with MockitoSugar with MockitoUtils with BeforeAndAfter with Matchers {

  private val helper = new GeneralHelper {}

  "GeneralHelper#numberOfTickets" should "always return the right number of tickets" in {

    helper.numberOfTickets(None) shouldBe 0
    helper.numberOfTickets(Some(Nil)) shouldBe 0
    helper.numberOfTickets(Some(List())) shouldBe 0
    helper.numberOfTickets(Some(List(createTicket()))) shouldBe 1
    helper.numberOfTickets(Some(List(createTicket(), createTicket()))) shouldBe 2

  }

}