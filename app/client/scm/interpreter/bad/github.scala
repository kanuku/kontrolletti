package client.scm.interpreter.impure

import client.RequestDispatcher
import client.scm.ScmOps
import scalaz.{~>, EitherT}
import scala.concurrent.Future

object github {

  type SafeFuture[A] = EitherT[Future, Throwable, A]

  def githubPlayInterpreter(reqDispatcher: RequestDispatcher): ScmOps ~> SafeFuture = new (ScmOps ~> SafeFuture) {
    import ScmOps._

    def apply[A](fa: ScmOps[A]): SafeFuture[A] = fa match {
      case CheckExist(conf, res) => ???
      case Get(conf, id) => ???
      case GetMulti(conf, from, start) => ???
    }
  }
}
