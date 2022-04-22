package org.w3.banana

sealed trait BananaException

case class VarNotFound(message: String) extends Exception(message) with BananaException
