
package utility

import org.scalatest.FunSuite
import play.api.test.Helpers._
import org.scalatest.Matchers
import test.util.ParsingData._
import scala.language.reflectiveCalls

/**
 * In this class you can find the individual tests for the regular expressions in UrlParser. <br/>
 * Note: Use the UrlParserMixedRegexExpressionsTest for testing mixed regular expressions.
 */
class UrlParserIndividualRegexExpressionTest extends FunSuite with Matchers with UrlParser {

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
    val diff2 = parsed.filterNot { x => Option(x) == None || x.isEmpty() }.filterNot { x => input.contains(x) }
    parsed.size shouldEqual input.size
    diff1 shouldBe empty
    diff2 shouldBe empty
  }

  // Very specific tests motivated by findings after first implementation
  test("projectAntecedent regex should also include /scm/") {
    val regex = s"$projectAntecedentRgx".r
    val input = "/scm/"
    val result = input match { case regex(result) => result case _ => "" }
    assert(result === input)
  }

}