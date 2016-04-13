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

object interpreter {
  import ScmOps._, Scm.{ReqParams, PagedResult}

  def githubInterpreter(client: Service[Request, Response]) = new (ScmOps ~> ScmResult) {

    def apply[A](fa: ScmOps[A]) = fa match {
      case BuildRequest(conf, scm, meta) =>
        val req = for {
          uri <- scm.resourceUri(conf, meta)
          token <- scm.accessToken(conf)
        } yield Request(uri = uri.copy(
          query = uri.query.withQueryParam("access_token", token.token)))
        ScmResult.fromDisjunction(req)
      case CheckExist(request) =>
        val action = client >=> checkOkK
        request.copy(method = Method.HEAD) |> action |> ScmResult.fromTask
      case GetCommit(request) =>
        val getCommit = client >=> readK[GithubCommit]
        request.copy(method = Method.GET) |> getCommit |> ScmResult.apply |> (_.map(_.run))
      case GetRepo(request) =>
        val getRepo = client >=> readK[GithubRepo]
        request.copy(method = Method.GET) |> getRepo |> ScmResult.apply |> (_.map(_.run))
      case GetCommitsPaged(request) =>
        val toPagedCommitsK = (readK[Vector[GithubCommit]] |@| readNextUriK){ (eCs, eUriOpt) =>
          for {
            commits <- eCs
            uriOpt  <- eUriOpt
          } yield PagedResult(commits.map(_.run), uriOpt)
        }
        val getCommits = client >=> toPagedCommitsK
        request.copy(method = Method.GET) |> getCommits |> ScmResult.apply
    }
  }

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
