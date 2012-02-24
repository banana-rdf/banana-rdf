package org.w3.rdf.n3

import collection.{immutable, mutable}
import org.w3.rdf._

case class Listener[RDF <: RDFDataType](val ops: RDFOperations[RDF]) {
  
  import ops._
  
  private val prefixs = new mutable.HashMap[String, RDF#IRI]
  val queue: mutable.Queue[RDF#Triple] = new mutable.Queue[RDF#Triple]()

  def send(a: RDF#Triple) = queue.enqueue(a)

  def addPrefix(name: String, value: RDF#IRI) {
    prefixs.put(name, value)
  }

  def setObject(obj: RDF#Node) {
    send(Triple(subject, verb, obj))
  }

  def resolve(pname: PName): Option[IRI] = {
    prefixs.get(pname.prefix).map{ case IRI(pre)=>IRI(pre + pname.name)}
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