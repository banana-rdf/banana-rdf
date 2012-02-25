package org.w3.rdf.n3

import collection.{immutable, mutable}
import org.w3.rdf._

/**
 * An agent that collects triples as they are built up and places them in a
 * structure which an other process can consume.
 * for a discussion see: https://bitbucket.org/pchiusano/nomo/issue/3/partial-results
 */
case class Listener[RDF <: RDFDataType](val ops: RDFOperations[RDF]) {
  
  import ops._
  
  private val prefixs = new mutable.HashMap[String, RDF#IRI]
  val queue: mutable.Queue[RDF#Triple] = new mutable.Queue[RDF#Triple]()

  // Perhaps it could send the triple to an agent here
  def send(a: RDF#Triple) = queue.enqueue(a)

  def addPrefix(name: String, value: RDF#IRI) {
    prefixs.put(name, value)
  }

  def setObject(obj: RDF#Node) {
    send(Triple(subject, verb, obj))
  }

  def resolve(pname: PName): Option[IRI] = {
    prefixs.get(pname.prefix).map{ case IRI(pre)=> IRI(pre + pname.name)}
  }

  def prefixes = prefixs.toMap

  var verb: RDF#IRI = _
  
  def setVerb(rel: RDF#IRI) {
    verb = rel
  }

  var subject: RDF#Node = _
  
  def setSubject(subj: RDF#Node) {
      subject = subj
  }

  
}

case class PName(prefix: String, name: String)