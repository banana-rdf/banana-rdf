package org.w3.rdf.n3

import collection.{immutable, mutable}
import org.w3.rdf._

case class Listener[RDF <: RDFDataType](val ops: RDFOperations[RDF]) {
  
  import ops._
  
  private val prefixs = new mutable.HashMap[String, RDF#IRI]
  val queue: mutable.Queue[RDF#Triple] = new mutable.Queue[RDF#Triple]()

  def send(a: RDF#Triple) = queue.enqueue(a)

  def addPrefix(nameSpace: String, uri: RDF#IRI) {
    prefixs.put(nameSpace, uri)
  }

  def setObject(obj: RDF#Node) {
    send(Triple(subject, verb, obj))
  }

  def prefixes = prefixs.toMap

  var verb: RDF#IRI = _
  
  def setVerb(rel: RDF#IRI) {
    verb = rel
  }

  var subject: RDF#Node = _
  
  def setSubject(subj: RDF#Node) =
    subj fold (
      iri => subject = iri,
      bnode => sys.error("not sure what you want"),
      literal => sys.error("not sure what you want")
    )
  
}

case class PName(prefix: String, name: String)