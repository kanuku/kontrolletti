package client.scm
package github

import argonaut.DecodeJson
import client.RequestDispatcher
import client.scm.github.githubmodel._
import client.scm.{Scm, ScmOps}
import client.scm.scmmodel.PagedResource
import client.scm.parser.github.GithubHeaderLink.nextUriParser
import org.http4s.{Request, Response, Service, Method, Status}
import org.http4s.util.CaseInsensitiveString
import org.http4s.argonaut._

import scalaz.{~>, \/, EitherT, Kleisli}
import scalaz.concurrent.Task
import scalaz.syntax.all._
import scalaz.syntax.std.option._

object interpreter {
  import ScmOps._, Scm.ReqParams

  def githubInterpreter(client: Service[Request, Response]) = new (ScmOps ~> ScmResult) {

    def apply[A](fa: ScmOps[A]) = fa match {
      case CheckExist(uri, token) =>
        val action = toRequestK(Method.HEAD) >=> client >=> checkOkK
        ReqParams(uri, token) |> action |> ScmResult.fromTask
      case GetCommit(uri, token) =>
        val getCommit = toRequestK(Method.GET) >=> client >=> readK[GithubCommit]
        ReqParams(uri, token) |> getCommit |> ScmResult.apply |> (_.map(_.run))
      case GetRepo(uri, token) =>
        val getRepo = toRequestK(Method.GET) >=> client >=> readK[GithubRepo]
        ReqParams(uri, token) |> getRepo |> ScmResult.apply |> (_.map(_.run))
      case GetCommitsPaged(uri, token) =>
        val toPagedCommitsK = (readK[Vector[GithubCommit]] |@| readNextUriK){ (eCs, eUriOpt) =>
          for {
            commits <- eCs
            uriOpt  <- eUriOpt
          } yield PagedResource(commits.map(_.run), uriOpt)
        }
        val getCommits = toRequestK(Method.GET) >=> client >=> toPagedCommitsK
        ReqParams(uri, token) |> getCommits |> ScmResult.apply
    }
  }

  def toRequest(method: Method, reqParams: ReqParams): Request =
    Request(method = method,
      uri = reqParams.uri.copy(
        query = reqParams.uri.query :+ ("access_token" -> reqParams.token.token.some)))

  def toRequestK(method: Method): Kleisli[Task, ReqParams, Request] =
    Kleisli(reqParams => toRequest(method, reqParams).pure[Task])

  val checkOkK: Kleisli[Task, Response, Boolean] =
    Kleisli((r: Response) => (r.status == Status.Ok).point[Task])

  def readK[A](implicit decoder: DecodeJson[A]): Kleisli[Task, Response, String \/ A] =
    Kleisli((res: Response) => res.attemptAs[A](jsonOf[A]).leftMap(_.message).run)

  def readNextUriK: Kleisli[Task, Response, String \/ Option[Uri]] = Kleisli { (res: Response) =>
    import fastparse.core.Parsed.{Success, Failure}
    val errOrUri =
      res.headers.get(CaseInsensitiveString("link")).map(_.value.right)
        .getOrElse(s"no link field in headers".left).flatMap { link =>
        nextUriParser.parse(link) match {
          case f@Failure(_,  _, _) =>
            s"error when try to parse link header: ${f.msg}".left
          case Success(v, _) =>
            v.right
        }
      }
    errOrUri.point[Task]
  }
}
