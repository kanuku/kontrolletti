

package v1.util

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import play.api.test.Helpers._
import org.scalatest.concurrent.AsyncAssertions.Waiter
import org.scalatest.Matchers
import v1.test.util.ParsingData

/**
 * In this class you can find the individual tests for the regular expressions in UrlParser .
 * If you add any test below, keep in mind that you should test only a single regular expression.
 */
class UrlParserIndividualRegexTest extends FunSuite with Matchers with UrlParser {

  //Github has none, but stash does it REST-STyle /projects/ 
  import ParsingData._

  test("parse (protocol) and get protocol") {
    val regex = s"$protocolRgx".r
    val parsed = for (protocol <- protocols) yield protocol match { case regex(result) => result }
    val diff = protocols.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }
  test("parse (user) and get user") {
    val regex = s"$userRgx".r
    val parsed = for (user <- users) yield user match { case regex(result) => result }
    val diff = users.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }
  test("parse (hostname+port) and get hostname+port") {
    val regex = s"$hostnameRgx".r
    val parsed = for (value <- fixture.hosts) yield value match { case regex(result) => result }
    val diff = fixture.hosts.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }
  test("parse ('/projects/') and get '/projects/'") {
    val regex = s"$projectAntecedentRgx".r
    val parsed = for (value <- projectAntecedents) yield value match { case regex(result) => result }
    val diff = projectAntecedents.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }

  test("parse (projects) and get projects ") {
    val regex = s"$projectRgx".r
    val parsed = for (value <- projects) yield value match { case regex(result) => result }
    val diff = projects.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }
    parsed.size shouldEqual projects.size
    diff shouldBe empty
  }

}