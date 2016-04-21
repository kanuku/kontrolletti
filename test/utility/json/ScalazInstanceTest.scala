package utility.json

import utility.json.scalazinstances._

import org.scalatest.FunSuite
import org.scalatest.Matchers
import org.scalatest.prop.Checkers

import argonaut._, Argonaut._
import scalaz.NonEmptyList


class ScalazInstancesTest extends FunSuite with Checkers with Matchers {

  test("NEL decodejson should decode non empty array") {
    check { (ln: List[Int]) =>
      val json = ln.asJson
      val result = json.as[NonEmptyList[Int]]

      if (ln.nonEmpty)
        result.value.get.list.toList == ln
      else
        result.failure.get._1 == "empty list"
    }
  }
}
