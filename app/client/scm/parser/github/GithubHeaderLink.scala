package client.scm.parser.github

import fastparse.all._
import scala.util.{Failure, Success, Try}

import java.net.URI

trait GithubHeaderLink {

  private val whiteSpacesParser: Parser[Unit] = P(CharIn(" \t\n") | StringIn("\r\n"))
  private val genericUriParser: Parser[URI] =
    P("<" ~ CharPred(_ != '>').rep.! ~ ">").flatMap { str =>
      Try(URI.create(str)) match {
        case Success(uri) => Pass.map(_ => uri)
        case Failure(uri) => Fail
      }

    }
  private val relParser: Parser[String] = P("rel=\"" ~ CharPred(_ != '"').rep.! ~ "\"")
  private val genericLinkRelParser: Parser[(URI, String)] =
    P(whiteSpacesParser.rep ~ genericUriParser ~ whiteSpacesParser.rep ~ ";" ~ whiteSpacesParser ~ relParser ~ ",".? ~ whiteSpacesParser.rep)
  val nextUriParser: Parser[Option[URI]] =
    genericLinkRelParser.rep.map(_.filter({ case (_, rel) =>
      rel == "next"
    }).headOption.map(_._1))
}
