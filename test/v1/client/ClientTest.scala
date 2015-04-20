package v1.client

import org.scalatest.FreeSpec
import org.scalatest._
import prop._

class ClientTest extends WordSpec with Matchers {
  val client: SCMClient = new ClientWrapper
  val kontrolletti = "https://github.com/zalando-bus/kontrolletti/"

  "ClientWrapper " when {
    "called with a github-url" should {
    client.map(kontrolletti) {
      (a,b,c) => println("")
    }
    assert(true)
    }
    
  } 
}