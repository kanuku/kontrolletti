package test.util

import model.Author

object TestUtils {

  def assertEitherIsNotNull[A, B](either: Either[A, B]) = {
    assert(either != null, "because either is null")
  }

  def assertEitherIsLeft[A, B](either: Either[A, B]) = {
    assert(either.isLeft, "because left is not true")
  }

  def assertEitherIsRight[A, B](either: Either[A, B]) = {
    assert(either.isRight, "because right is not true")
  }
  def assertEitherLeftEquals[A, B](either: Either[A, B], value:A) = {
	  assert(either.left== value, "because right is not true")
  }

}
