package org.w3.banana

import java.net.{ URI => jURI }

object URIHelper {

    def resolve[Rdf <: RDF](uri: Rdf#URI, str: String)(implicit ops: RDFOperations[Rdf]): Rdf#URI = {
      val juri = new jURI(uri.toString).resolve(str)
      ops.makeUri(juri.toString)
    }

    def relativize[Rdf <: RDF](uri: Rdf#URI, other: Rdf#URI)(implicit ops: RDFOperations[Rdf]): Rdf#URI = {
      val juri = new jURI(uri.toString).relativize(new jURI(other.toString))
      ops.makeUri(juri.toString)
    }

}
