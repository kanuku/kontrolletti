package client.scm
package stash

import org.http4s.{Request, Response, Service}
import client.oauth.OAuthAccessToken
import scalaz.~>

object interpreter {
  import ScmOps._

  def stashInterpreter(client: Service[Request, Response], authTokenClient: Service[Unit, OAuthAccessToken]) = new (ScmOps ~> ScmResult) {

    def apply[A](fa: ScmOps[A]) = fa match {
      case BuildRequest(conf, scm, meta, initOpt) =>
        val req = for {
          defaultUri <- scm.resourceUri(conf, meta)
          user <- scm.user(conf)
          token <- scm.accessToken(conf)
        } yield ()
        ???
      case _ => ???
    }
  }
}
