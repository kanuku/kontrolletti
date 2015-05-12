

package v1.util

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import play.api.test.Helpers._
import org.scalatest.concurrent.AsyncAssertions.Waiter
import org.scalatest.Matchers

@RunWith(classOf[JUnitRunner])
class UrlParserTest extends FunSuite with Matchers with UrlParser {

  //  private val stashHost = "stash.zalando.net"
  //  private val stashProject = "ZKON_-A"
  //  private val stashRepo = "asdfh--now-test_af"

  private val projectLinks = List("","/projects/")
  private val users = List("", "git@", "fbenjamin@")
  private val protocols = List("", "http://", "http://", "https://", "ssh://")
  private val ports = List("", ":8080", ":80")
  private val hostnames = List("stash", "stash-server",
    "stash.net", "stash-zalando",
    "live-stash-zalando", "stash.cd.zalando")

  def fixture = new {
    val protocolWithUser = for {
      protocol <- protocols
      user <- users
    } yield (protocol + user)
    val hosts =
      for {
        host <- hostnames
        port <- ports
      } yield (host + port)

    val hostsWithProtocol =
      for {
        protocol <- protocols
        host <- hostnames
        port <- ports
      } yield (protocol + host + port)
  }

  test("parse (protocol+hostname+port) and get (hostname+port) without protocol") {
    val parsed = for (host <- fixture.hostsWithProtocol) yield parse(host)._1
    val diff = fixture.hosts.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }

  test("parse (hostname+port) and get (hostname+port)") {
    val parsed = for (host <- fixture.hosts) yield parse(host)._1
    val diff = fixture.hosts.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }

  test("parse (host) and get (hostname+port)") {
    val parsed = for (host <- fixture.hosts) yield parse(host)._1
    val diff = fixture.hosts.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }
  test("parse (protocol) and get protocol") {
    val regex = s"$protocol".r
    val parsed = for (protocol <- protocols) yield protocol match {
      case regex(result) => result
    }
    val diff = protocols.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }
  test("parse (user) and get user") {
    val regex = s"$user".r
    val parsed = for (user <- users) yield user match {
      case regex(result) => result
    }
    val diff = users.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }
  
  test ("parse (/projects/) and get user"){
    val regex = s"$projectLink".r
//    val parsed = 
    
  }
}