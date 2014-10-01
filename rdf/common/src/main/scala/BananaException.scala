package org.w3.banana

trait BananaException extends Exception

case class FailedConversion(message: String) extends Exception(message) with BananaException

case class WrongExpectation(message: String) extends Exception(message) with BananaException

case class WrappedThrowable(t: Throwable) extends Exception(t) with BananaException

case class NoReader(mimetype: String) extends Exception("No RDFReader for " + mimetype) with BananaException

case class LocalNameException(message: String) extends Exception(message) with BananaException

case class VarNotFound(message: String) extends Exception(message) with BananaException

case class StoreProblem(t: Throwable) extends Exception(t) with BananaException

case object NotPureFragment extends Exception("not a pure fragment URI") with BananaException

case class BananaTimeout(te: java.util.concurrent.TimeoutException) extends Exception(te) with BananaException
