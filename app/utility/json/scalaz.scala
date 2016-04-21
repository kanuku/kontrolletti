package utility.json

import argonaut._, Argonaut._
import scalaz.NonEmptyList

object scalazinstances {

  implicit def nelDecodeJson[A](implicit e: DecodeJson[A]): DecodeJson[NonEmptyList[A]] =
    DecodeJson(c => c.as[List[A]] flatMap {
      case x :: xs => DecodeResult.ok(NonEmptyList(x, xs: _*))
      case _       => DecodeResult.fail("empty list", c.history)
    })
}
