package client

import play.api.Play.current
import play.api.libs.ws.WS
import play.api.libs.ws.WSRequestHolder
import javax.inject.Singleton
/**
 * @author fbenjamin
 *
 * This class has been created to simplify unit-tests.
 * Motivation: <br>
 * SCMImpl was using play's @play.api.libs.ws.WS(Singleton) directly
 * which forced us to make complex unit-tests.
 * By exporting those same calls to this separate trait(interface),
 * we(you) can easily mock the calls to isolate and simplify the unit tests.
 *
 *
 */
trait RequestDispatcher {

  def requestHolder(url: String): WSRequestHolder
}

@Singleton
class RequestDispatcherImpl extends RequestDispatcher {

  def requestHolder(url: String): WSRequestHolder = {
    WS.url(url)
  }
}



