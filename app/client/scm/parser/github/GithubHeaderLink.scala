package client.scm
package parser.github

import fastparse.all._
import scala.util.{Failure, Success, Try}
import scalaz.\/-

object GithubHeaderLink {

  private val whiteSpacesParser: Parser[Unit] = P(CharIn(" \t\n") | StringIn("\r\n"))
  private val genericUriParser: Parser[Uri] =
    P("<" ~ CharPred(_ != '>').rep.! ~ ">").flatMap { str =>
      Uri.fromString(str) match {
        case \/-(uri) => Pass.map(_ => uri)
        case _ => Fail
      }
    }
  private val relParser: Parser[String] = P("rel=\"" ~ CharPred(_ != '"').rep.! ~ "\"")
  private val genericLinkRelParser: Parser[(Uri, String)] =
    P(whiteSpacesParser.rep ~ genericUriParser ~ whiteSpacesParser.rep ~ ";" ~ whiteSpacesParser ~ relParser ~ ",".? ~ whiteSpacesParser.rep)
  val nextUriParser: Parser[Option[Uri]] =
    genericLinkRelParser.rep.map(_.filter({ case (_, rel) =>
      rel == "next"
    }).headOption.map(_._1))
}
