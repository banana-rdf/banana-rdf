package org.w3.banana.exceptions

trait BananaException

class URIException(message: String) extends Exception(message) with BananaException

