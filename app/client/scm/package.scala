package client

import scalaz.{\/, EitherT}
import scalaz.concurrent.Task
import scalaz.syntax.either._

package object scm {

  type Uri = org.http4s.Uri
  val Uri = org.http4s.Uri

  type ScmResult[A] = EitherT[Task, String, A]
  object ScmResult {
    def apply[A](tsva: Task[String \/ A]): ScmResult[A] =
      EitherT.eitherT(tsva)
    def fromTask[A](ta: Task[A]): ScmResult[A] =
      EitherT.eitherT(ta.map(_.right))
    def fromDisjunction[A](va: String \/ A): ScmResult[A] =
      EitherT.fromDisjunction[Task].apply(va)
  }
}
