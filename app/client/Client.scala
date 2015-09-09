package client

import play.api.Play.current
import play.api.libs.ws.WSRequest
import javax.inject.Singleton
import javax.inject.Inject
import play.api.libs.ws.WSClient
import play.api.libs.ws.WSRequest
import com.google.inject.ImplementedBy
import play.api.Logger
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

  def requestHolder(url: String): WSRequest
}

@Singleton
class RequestDispatcherImpl @Inject() (client: WSClient) extends RequestDispatcher {
  val logger: Logger = Logger(this.getClass())
  def requestHolder(url: String): WSRequest = {
    logger.info(s"Creating an dispatcher for $url")
    client.url(url).withRequestTimeout(3000)
  }
}



