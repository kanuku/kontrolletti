package client.scm.interpreter

import client.scm.Scm.ConfError


sealed trait InterpreterError extends Product with Serializable
final case class InterpreterException(ex: Throwable)
    extends InterpreterError
final case class ConfigError(err: ConfError) extends InterpreterError
