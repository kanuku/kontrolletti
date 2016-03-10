package utility.parser

import scala.language.implicitConversions

sealed trait ParseResult[A]
case class ParseError[A](detail: String) extends ParseResult[A]
case class Result[A](result: A, rest: List[Char]) extends ParseResult[A]

trait Parser[A] { self =>

  def parse: List[Char] => ParseResult[A]

  def map[B](f: A => B): Parser[B] = new Parser[B] {
    def parse = self.parse andThen {
      case Result(result, rest) => Result(f(result), rest)
      case ParseError(detail)   => ParseError(detail)
    }
  }

  def flatMap[B](f: A => Parser[B]): Parser[B] = new Parser[B] {
    def parse = self.parse andThen {
      case Result(result, rest) => f(result).parse(rest)
      case ParseError(detail)   => ParseError(detail)
    }
  }

  /** try self first, if failed, try to parse with given Parser. */
  def or(p: Parser[A]): Parser[A] = new Parser[A] {
    def parse = { in =>
      self.parse(in) match {
        case result: Result[A] => result
        case _: ParseError[A]  => p.parse(in)
      }
    }
  }

  /** use self to parse and ignore parsed result, use given Parser to parse rest */
  def chooseRight(p: Parser[A]) = flatMap(_ => p)
}

object Parser extends DefaultParsers {
  // FIXME: when introduce scalaz, provide a MonadPlus instance

  implicit def toCharList(str: Seq[Char]): List[Char] =
    str.toList

  /** construct a Parser[A] that will always succeed with given value */
  def valueParser[A](a: A): Parser[A] = new Parser[A] {
    def parse = i => Result(a, i)
  }

  /** construct a Parser[A] will always fail with given message */
  def failed[A](detail: String): Parser[A] = new Parser[A] {
    def parse = _ => ParseError[A](detail)
  }

  val character: Parser[Char] = new Parser[Char] {
    def parse = {
      case h :: t => Result(h, t)
      case _      => ParseError("Input is empty.")
    }
  }

  val nonEmptyString: Parser[String] =
    list1(character).map(_.mkString)

  def list1[A](p: Parser[A]): Parser[List[A]] =
    for {
      h <- p
      t <- list(p)
    } yield h :: t

  def list[A](p: Parser[A]): Parser[List[A]] =
    list1(p) or valueParser(List.empty[A])

  def satisfy[A](
    pred: A => Boolean,
    desc: String = "default description for rule")(
    p: Parser[A]
  ): Parser[A] =
    p flatMap { result =>
      if (pred(result)) valueParser(result)
      else failed(s"Parsed value $result does not satisfy predicate: $desc")
    }

  def is(c: Char): Parser[Char] =
    satisfy(((x: Char) => x == c), s"check if character is [$c]")(character)

  val space: Parser[Char] = is(' ')

  val spaces: Parser[List[Char]] = list(space)

  val spaces1: Parser[List[Char]] = list1(space)

  def between[A](open: Char, close: Char)(p: Parser[A]): Parser[A] = for {
    _ <- is(open)
    a <- p
    _ <- is(close)
  } yield a

  def token[A](p: Parser[A]): Parser[A] = for {
    a <- p
    _ <- spaces
  } yield a

  // FIXME: rewrite use sequence when scalaz is introduced
  def stringVal(str: String): Parser[String] = {
    val parsers = str.toList.map(is(_))
    val charListParser =
      parsers.foldLeft(valueParser(List.empty[Char])) { (acc, curr) =>
      for {
        accumulated <- acc
        current     <- curr
      } yield accumulated :+ current
      }
    charListParser.map(_.mkString)
  }
}
