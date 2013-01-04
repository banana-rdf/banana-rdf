package org.w3.banana

import java.net.{URI => jURI}

object URIHelper {
  val regex = "#".r

  def resolve[Rdf <: RDF](uri: Rdf#URI, str: String)(implicit ops: RDFOps[Rdf]): Rdf#URI = {
    val juri = if (""==str) new jURI(regex.split(uri.toString)(0))
               else new jURI(uri.toString).resolve(str)
    ops.makeUri(juri.toString)
  }

  def relativize[Rdf <: RDF](uri: Rdf#URI, other: Rdf#URI)(implicit ops: RDFOps[Rdf]): Rdf#URI = {
    val juri = new jURI(uri.toString).relativize(new jURI(other.toString))
    ops.makeUri(juri.toString)
  }

}
