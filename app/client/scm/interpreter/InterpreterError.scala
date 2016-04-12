package client.scm.interpreter

final case class InterpreterError(msg: String, ex: Option[Throwable])
