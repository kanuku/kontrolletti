package client

import play.api.Play.current
import play.api.libs.ws.WSRequest
import javax.inject.Singleton
import javax.inject.Inject
import play.api.libs.ws.WSClient
import play.api.libs.ws.WSRequest
import com.google.inject.ImplementedBy
import play.api.Logger
import configuration.GeneralConfiguration

/**
 * @author fbenjamin
 *
 * This class has been created to simplify unit-tests.
 * Motivation: <br>
 * Kontrolletti was using play's @play.api.libs.ws.WS(Singleton) directly
 * which forced us to make complex unit-tests.
 * By exporting those same calls to this separate trait(interface),
 * we(you) can easily mock the calls to isolate and simplify the unit tests.
 */
trait RequestDispatcher {
  def requestHolder(url: String): WSRequest
}

@Singleton
class RequestDispatcherImpl @Inject() (client: WSClient, config: GeneralConfiguration) extends RequestDispatcher {
  val logger: Logger = Logger(this.getClass())

  val client2 = {
    val builder = new com.ning.http.client.AsyncHttpClientConfig.Builder()
    builder.setAcceptAnyCertificate(true)
    new play.api.libs.ws.ning.NingWSClient(builder.build())
  }

  def requestHolder(url: String): WSRequest = {
    logger.info(s"Creating an dispatcher for $url")
    client2.url(url).withRequestTimeout(config.defaultClientTimeout.toLong)
  }

}
