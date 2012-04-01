package org.w3.linkeddata

sealed trait LDError

case object Timeout extends LDError
case class ParsingError(why: String) extends LDError
case class UnknownContentType(contentType: String) extends LDError
