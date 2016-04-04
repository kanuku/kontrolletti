package client.scm.interpreter.bad

import client.RequestDispatcher
import client.scm.ScmOps
import client.scm.interpreter.{InterpreterError, ConfigError, InterpreterException}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scalaz.{~>, \/, EitherT}
import scalaz.std.scalaFuture._
import scalaz.syntax.all._



object github {

  type SafeFuture[A] = EitherT[Future, InterpreterError, A]

  def githubPlayInterpreter(reqDispatcher: RequestDispatcher)(implicit
    ec: ExecutionContext
  ): ScmOps ~> SafeFuture = new (ScmOps ~> SafeFuture) {
    import ScmOps._

    def apply[A](fa: ScmOps[A]): SafeFuture[A] = fa match {
      case c @ CheckExist(conf, res) =>
        val resourceUrl = c.scm.resourceUri(conf, res).leftMap(ConfigError)
        val token = c.scm.accessToken(conf).leftMap(ConfigError)
        val check = (for {
          url <- resourceUrl
          t   <- token
        } yield reqDispatcher.requestHolder(url.toString).withQueryString("access_token" -> t).head.map(_.status == 200)).sequence
        // TODO: handle exception
        val checkWRecover = check recover {
          case e if NonFatal(e) => InterpreterException(e).left
        }
        EitherT.eitherT(checkWRecover)
      case g @ Get(conf, id) => ???
      case gm @ GetMulti(conf, from, start) => ???
    }
  }
}
