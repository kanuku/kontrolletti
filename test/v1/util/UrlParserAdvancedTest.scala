package v1.util

import org.scalatest.FunSuite
import org.scalatest.Matchers
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import play.api.test.Helpers._
import v1.test.util.ParsingData

class UrlParserAdvancedTest extends FunSuite with Matchers with UrlParser {
  import ParsingData._

  test("parse(protocol+user+hostname+project) and get (protocol+user+hostname+project)") {
    val input = fixture.protocolUserHostAntecedents
    println("INPUT ==" + input)
    val regex = s"$protocolRgx$userRgx$hostnameRgx$projectAntecedentRgx".r
    val parsed = for (value <- input if !value.isEmpty()) yield value match {
      case regex(protocol, user, host, antecedent) =>
        println("GOOD ==" + value)
        concat(protocol, user, host, antecedent)
      //        case _ => println("ERROR =="+value)
    }
    println("## INPUT ##" + input)
    println("## PARSED ##" + parsed)
    val diff = input.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }

  test("parse(protocol+user+hostname) and get (protocol+user+hostname)") {
    val input = fixture.protocolUserHosts
    val regex = s"$protocolRgx$userRgx$hostnameRgx".r
    val parsed = for (value <- input) yield value match {
      case regex(protocol, user, host) => concat(protocol, user, host)
    }
    val diff = input.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }
  test("parse(protocol+user) and get (protocol+user)") {
    val input = fixture.protocolUsers
    val regex = s"$protocolRgx$userRgx".r
    val parsed = for (value <- input if !value.isEmpty()) yield value match {
      case regex(protocol, user) => concat(protocol, user)
    }
    val diff = input.filterNot { x => x.isEmpty() }.filterNot { x => parsed.contains(x) }
    diff shouldBe empty
  }

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