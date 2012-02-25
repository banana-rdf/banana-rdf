package org.w3.rdf.n3

import collection.{immutable, mutable}
import org.w3.rdf._

/**
 * An agent that collects triples as they are built up and places them in a
 * structure which an other process can consume.
 * for a discussion see: https://bitbucket.org/pchiusano/nomo/issue/3/partial-results
 */
case class Listener[Rdf <: RDF](val ops: RDFOperations[Rdf]) {
  
  import ops._
  
  private val prefixs = new mutable.HashMap[String, Rdf#IRI]
  val queue: mutable.Queue[Rdf#Triple] = new mutable.Queue[Rdf#Triple]()

  // Perhaps it could send the triple to an agent here
  def send(a: Rdf#Triple) = queue.enqueue(a)

  def addPrefix(name: String, value: Rdf#IRI) {
    prefixs.put(name, value)
  }

  def setObject(obj: Rdf#Node) {
    send(Triple(subject, verb, obj))
  }

  def resolve(pname: PName): Option[IRI] = {
    prefixs.get(pname.prefix).map{ case IRI(pre)=> IRI(pre + pname.name)}
  }

  def prefixes = prefixs.toMap

  var verb: Rdf#IRI = _
  
  def setVerb(rel: Rdf#IRI) {
    verb = rel
  }

  var subject: Rdf#Node = _
  
  def setSubject(subj: Rdf#Node) {
      subject = subj
  }

  
}

case class PName(prefix: String, name: String)