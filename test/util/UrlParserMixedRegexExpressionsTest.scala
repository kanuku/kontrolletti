package util

import scala.concurrent._
import org.scalatest.Distributor
import org.scalatest.FunSuite
import org.scalatest.Matchers
import org.scalatest.WordSpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import play.api.test.Helpers._
import test.util.ParsingData
import scala.util.matching.Regex
import test.util.TestUtils
import utility.UrlParser

/**
 * This class tests for parsing URL composed of corner cases.
 * The URL's are being tested simultaneous
 */
class UrlParserMixedRegexExpressionsTest extends FunSuite with Matchers with UrlParser with ScalaFutures {
import utility.UrlParser;
  import ParsingData._
import TestUtils._
  /**
   * This test has 712800 URLS to test, that is why the URLS are being tested in parallel.
   */
  test("extract(protocol+user+hostname+antecedent+project+antecedent+repo+repoSucceeders) and get (protocol+user+hostname+antecedent+project+antecedent+repo+repoSucceeders)") {
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
   * This test has 118800 URLS to test, that is why the URLS are being tested in parallel.
   *
   */
  test("extract(protocol+user+hostname+antecedent+project+antecedent+repo) and get (protocol+user+hostname+antecedent+project+antecedent+repo)") {
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

  test("extract(protocol+user+hostname+antecedent+project+antecedent) and get (protocol+user+hostname+antecedent+project+antecedent)") {
    val input = fixture.protocolUserHostAntecedentsProjectAntecedents
    val regex = s"$protocolRgx$userRgx$hostnameRgx$projectAntecedentRgx$projectRgx$repoAntecedentRgx".r
    val parsed = for (value <- input if !value.isEmpty()) yield value match {
      case regex(protocol, user, host, prjAntecedent, project, repoAntecedent) =>
        concat(protocol, user, host, prjAntecedent, project, repoAntecedent)
    }
    val diff = input.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }

  test("extract(protocol+user+hostname+antecedent+project) and get (protocol+user+hostname+antecedent+project)") {
    val input = fixture.protocolUserHostAntecedentsProjects
    val regex = s"$protocolRgx$userRgx$hostnameRgx$projectAntecedentRgx$projectRgx".r
    val parsed = for (value <- input if !value.isEmpty()) yield value match { case regex(protocol, user, host, antecedent, project) => concat(protocol, user, host, antecedent, project) }
    val diff = input.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }

  test("extract(protocol+user+hostname+antecedent) and get (protocol+user+hostname+antecedent)") {
    val input = fixture.protocolUserHostAntecedents
    val regex = s"$protocolRgx$userRgx$hostnameRgx$projectAntecedentRgx".r
    val parsed = for (value <- input if !value.isEmpty()) yield value match { case regex(protocol, user, host, antecedent) => concat(protocol, user, host, antecedent) }
    val diff = input.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }

  test("extract(protocol+user+hostname) and get (protocol+user+hostname)") {
    val input = fixture.protocolUserHosts
    val regex = s"$protocolRgx$userRgx$hostnameRgx".r
    val parsed = for (value <- input) yield value match { case regex(protocol, user, host) => concat(protocol, user, host) }
    val diff = input.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }

    diff shouldBe empty
  }
  test("extract(protocol+user) and get (protocol+user)") {
    val input = fixture.protocolUsers
    val regex = s"$protocolRgx$userRgx".r
    val parsed = for (value <- input if !value.isEmpty()) yield value match { case regex(protocol, user) => concat(protocol, user) }
    val diff = input.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }

  test("parse (null) and get empty tuple") {
    val either = extract(null)
    assertEitherIsNotNull(either)
    assertEitherIsLeft(either)
    assert(either.left.get == "Repository-url should not be empty/null")
  }
  test("parse (empty String) and get empty tuple") {
    val either = extract("")
    assertEitherIsNotNull(either)
    assertEitherIsLeft(either)
    assert(either.left.get == "Repository-url should not be empty/null")
  }
  test("parse (NoneURL) and get empty tuple") {
    val url="asdfasdfölakjsdfölkajsdfölkj1230790823702934857"
    val either = extract(url)
    assertEitherIsNotNull(either)
    assertEitherIsLeft(either)
    assert(either.left.get == s"Could not parse $url")
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