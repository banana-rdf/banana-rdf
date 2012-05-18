package org.w3.banana

sealed abstract class BananaException(message: String) extends Exception(message)

case class FailedConversion(message: String) extends BananaException(message)
case class WrongExpectation(message: String) extends BananaException(message)
