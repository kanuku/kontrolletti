

package util

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import play.api.test.Helpers._
import org.scalatest.concurrent.AsyncAssertions.Waiter
import org.scalatest.Matchers
import test.util.ParsingData
import utility.UrlParser

/**
 * In this class you can find the individual tests for the regular expressions in UrlParser. <br/>
 * Note: Use the UrlParserMixedRegexExpressionsTest for testing mixed regular expressions.
 */
class UrlParserIndividualRegexExpressionTest extends FunSuite with Matchers with UrlParser {

  //Github has none, but stash does it REST-STyle /projects/ 
import utility.UrlParser;
import ParsingData._

  test("parse (protocol) and get protocol") {
    val regex = s"$protocolRgx".r
    val input = users
    val parsed = for (value <- protocols if !value.isEmpty()) yield value match { case regex(result) => result }
    val diff1 = protocols.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }
    val diff2 = parsed.filterNot { x => protocols.contains(x) }
    diff1 shouldBe empty
    diff2 shouldBe empty
  }

  test("parse (user) and get user") {
    val regex = s"$userRgx".r
    val input = users
    val parsed = for (value <- input if !value.isEmpty()) yield value match { case regex(result) => result }
    val diff1 = input.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }
    val diff2 = parsed.filterNot { x => x.isEmpty() }.filterNot { x => input.contains(x) }
    diff1 shouldBe empty
    diff2 shouldBe empty
  }

  test("parse (hostname+port) and get hostname+port") {
    val regex = s"$hostnameRgx".r
    val input = fixture.hosts
    val parsed = for (value <- input) yield value match { case regex(result) => result }
    val diff1 = input.filterNot { x => parsed.contains(x) }
    val diff2 = parsed.filterNot { x => input.contains(x) }
    diff1 shouldBe empty
    diff2 shouldBe empty
  }

  test("parse project antecedent ('/projects/' and '/' ) and get '/projects/' and '/' ") {
    val regex = s"$projectAntecedentRgx".r
    val input = projectAntecedents
    val parsed = for (value <- input) yield value match { case regex(result) => result }
    val diff1 = input.filterNot { x => parsed.contains(x) }
    val diff2 = parsed.filterNot { x => input.contains(x) }
    parsed.size shouldEqual input.size
    diff1 shouldBe empty
    diff2 shouldBe empty
  }

  test("parse (projects) and get projects ") {
    val regex = s"$projectRgx".r
    val input = projects
    val parsed = for (value <- input) yield value match { case regex(result) => result }
    val diff1 = input.filterNot { x => parsed.contains(x) }
    val diff2 = parsed.filterNot { x => input.contains(x) }
    parsed.size shouldEqual input.size
    diff1 shouldBe empty
    diff2 shouldBe empty
  }
  test("parse project precedent and get (projects ))") {
	  val regex = s"$projectRgx".r
			  val input = projects
			  val parsed = for (value <- input) yield value match { case regex(result) => result }
	  val diff1 = input.filterNot { x => parsed.contains(x) }
	  val diff2 = parsed.filterNot { x => input.contains(x) }
	  parsed.size shouldEqual input.size
	  diff1 shouldBe empty
	  diff2 shouldBe empty
  }

  test("parse repo antecedent ('/repos/ and '/') and get '/repos/' and '/'") {
    val regex = s"$repoAntecedentRgx".r
    val input = repoAntecedents
    val parsed = for (value <- input) yield value match { case regex(result) => result }
    val diff1 = input.filterNot { x => parsed.contains(x) }
    val diff2 = parsed.filterNot { x => input.contains(x) }
    parsed.size shouldEqual input.size
    diff1 shouldBe empty
    diff2 shouldBe empty
  }

  test("parse (repositories) and get repositories ") {
    val regex = s"$repoRgx".r
    val input = repos
    val parsed = for (value <- input) yield value match { case regex(result) => result }
    val diff1 = input.filterNot { x => parsed.contains(x) }
    val diff2 = parsed.filterNot { x => input.contains(x) }
    parsed.size shouldEqual input.size
    diff1 shouldBe empty
    diff2 shouldBe empty
  }

  test("parse repo succeeders ('/' and '/browse/') and get  ('/' and '/browse/')") {
    val regex = s"$repoSucceederRgx".r
    val input = repoSucceeders
    val parsed = for (value <- input) yield value match { case regex(result) => result }
    val diff1 = input.filterNot { x => x != "null" }.filterNot { x => parsed.contains(x) }
    val diff2 = parsed.filterNot { x => x == null || x.isEmpty() }.filterNot { x => input.contains(x) }
    parsed.size shouldEqual input.size
    diff1 shouldBe empty
    diff2 shouldBe empty
  }
 
}