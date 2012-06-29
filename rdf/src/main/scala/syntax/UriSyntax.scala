package org.w3.banana.syntax

import org.w3.banana._
import java.net.{ URI => jURI }

trait URISyntax[Rdf <: RDF] {
  this: RDFOperationsSyntax[Rdf] =>

  implicit def uriWrapper(uri: Rdf#URI): URIW = new URIW(uri)

  class URIW(uri: Rdf#URI) {

    def getString: String = ops.fromUri(uri)

    def fragmentLess: Rdf#URI = {
      val uriString = ops.fromUri(uri)
      val juri = new jURI(uriString)
      import juri._
      val uriNoFrag = new jURI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, null)
      ops.makeUri(uriNoFrag.toString)
    }

    def fragment(frag: String): Rdf#URI = {
      val uriString = ops.fromUri(uri)
      val juri = new jURI(uriString)
      import juri._
      val uriWithFrag = new jURI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, frag)
      ops.makeUri(uriWithFrag.toString)
    }

    def fragment: Option[String] = {
      val uriString = ops.fromUri(uri)
      val juri = new jURI(uriString)
      Option(juri.getFragment)
    }

  }

}

object URISyntax {

}
