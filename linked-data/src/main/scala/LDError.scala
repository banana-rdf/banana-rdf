package org.w3.linkeddata

sealed trait LDError

case object Timeout extends LDError
case object ParsingError extends LDError
case object Unknown extends LDError
