package v1.util

import scala.concurrent._
import org.scalatest.Distributor
import org.scalatest.FunSuite
import org.scalatest.Matchers
import org.scalatest.WordSpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import play.api.test.Helpers._
import v1.test.util.ParsingData
import scala.util.matching.Regex
class UrlParserMixedRegexExpressionsTest extends FunSuite with Matchers with UrlParser with ScalaFutures {
  import ParsingData._

  /**
   * This test has 712800 URLS to test, that is why a future is being used here.
   */
  test("parse(protocol+user+hostname+antecedent+project+antecedent+repo+repoSucceeders) and get (protocol+user+hostname+antecedent+project+antecedent+repo+repoSucceeders)") {
    for (input <- fixture.protocolUserHostAntecedentsProjectAntecedentReposRepoSucceeders.grouped(30)) {
      val test = executeParallellRepoUrlWithSucceeder(input)
      whenReady(test) { result =>
        val diff = input.filterNot { x => result.contains(x) }
        diff shouldBe empty
      }
    }
    def executeParallellRepoUrlWithSucceeder(input: List[String]): Future[List[String]] = Future.successful {
      val regex = s"$protocolRgx$userRgx$hostnameRgx$projectAntecedentRgx$projectRgx$repoAntecedentRgx$repoRgx$repoSucceederRgx".r
      for (value <- input) yield value match {
        case regex(protocol, user, host, prjAntecedent, project, repoAntecedent, repo, succeeder) =>
          concat(protocol, user, host, prjAntecedent, project, repoAntecedent, repo, succeeder)
      }
    }

  }

  /**
   * This test has 118800 URLS to test, that is why a future is being used here.
   */
  test("parse(protocol+user+hostname+antecedent+project+antecedent+repo) and get (protocol+user+hostname+antecedent+project+antecedent+repo)") {
    for (input <- fixture.protocolUserHostAntecedentsProjectAntecedentRepos.grouped(30)) {
      val test = executeParallellRepoUrl(input)
      whenReady(test) { result =>
        val diff = input.filterNot { x => result.contains(x) }
        diff shouldBe empty
      }
    }
    def executeParallellRepoUrl(input: List[String]): Future[List[String]] = Future.successful {
      val regex = s"$protocolRgx$userRgx$hostnameRgx$projectAntecedentRgx$projectRgx$repoAntecedentRgx$repoRgx".r
      for (value <- input) yield value match {
        case regex(protocol, user, host, prjAntecedent, project, repoAntecedent, repo) =>
          concat(protocol, user, host, prjAntecedent, project, repoAntecedent, repo)
      }
    }
  }

  test("parse(protocol+user+hostname+antecedent+project+antecedent) and get (protocol+user+hostname+antecedent+project+antecedent)") {
    val input = fixture.protocolUserHostAntecedentsProjectAntecedents
    val regex = s"$protocolRgx$userRgx$hostnameRgx$projectAntecedentRgx$projectRgx$repoAntecedentRgx".r
    val parsed = for (value <- input if !value.isEmpty()) yield value match {
      case regex(protocol, user, host, prjAntecedent, project, repoAntecedent) =>
        concat(protocol, user, host, prjAntecedent, project, repoAntecedent)
    }
    val diff = input.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }

  test("parse(protocol+user+hostname+antecedent+project) and get (protocol+user+hostname+antecedent+project)") {
    val input = fixture.protocolUserHostAntecedentsProjects
    val regex = s"$protocolRgx$userRgx$hostnameRgx$projectAntecedentRgx$projectRgx".r
    val parsed = for (value <- input if !value.isEmpty()) yield value match { case regex(protocol, user, host, antecedent, project) => concat(protocol, user, host, antecedent, project) }
    val diff = input.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }

  test("parse(protocol+user+hostname+antecedent) and get (protocol+user+hostname+antecedent)") {
    val input = fixture.protocolUserHostAntecedents
    val regex = s"$protocolRgx$userRgx$hostnameRgx$projectAntecedentRgx".r
    val parsed = for (value <- input if !value.isEmpty()) yield value match { case regex(protocol, user, host, antecedent) => concat(protocol, user, host, antecedent) }
    val diff = input.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }

  test("parse(protocol+user+hostname) and get (protocol+user+hostname)") {
    val input = fixture.protocolUserHosts
    val regex = s"$protocolRgx$userRgx$hostnameRgx".r
    val parsed = for (value <- input) yield value match { case regex(protocol, user, host) => concat(protocol, user, host) }
    val diff = input.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }

    diff shouldBe empty
  }
  test("parse(protocol+user) and get (protocol+user)") {
    val input = fixture.protocolUsers
    val regex = s"$protocolRgx$userRgx".r
    val parsed = for (value <- input if !value.isEmpty()) yield value match { case regex(protocol, user) => concat(protocol, user) }
    val diff = input.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }

  /**
   * Accepts one/multiple strings and joins them together. <br/>
   * Removes null-strings to avoid "null" value in the result string.
   *
   */

  def concat(strings: String*) = {
    strings.reduceLeft { (val1, val2) =>
      {
        if (val1 == null)
          if (val2 == null) null
          else val2
        else if (val2 == null) val1
        else val1 + val2
      }
    }

  }

}