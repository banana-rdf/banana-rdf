package org.w3.banana.syntax

import org.w3.banana._

trait URISyntax[Rdf <: RDF] {
this: RDFOperationsSyntax[Rdf] =>

  implicit def uriWrapper(uri: Rdf#URI): URIW = new URIW(uri)

  class URIW(uri: Rdf#URI) {

    def getString: String = ops.fromUri(uri)

    def supportDocument: Rdf#URI = {
      val uriString = ops.fromUri(uri)
      val jUri = new java.net.URI(uriString)
      import jUri._
      val uriNoFrag = new java.net.URI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, null)
      ops.makeUri(uriNoFrag.toString)
    }

  }

}

object URISyntax {



}
