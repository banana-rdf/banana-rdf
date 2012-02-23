package org.w3.rdf.n3

import collection.{immutable, mutable}
import org.w3.rdf.RDFModule

case class Listener[M <: RDFModule](val m: M) {
  private val prefixs = new mutable.HashMap[String, m.IRI]
  val queue: mutable.Queue[m.Triple] = new mutable.Queue[m.Triple]()

  def send(a: Triple) = queue.enqueue(a)

  def addPrefix(nameSpace: String, uri: m.IRI) {
    prefixs.put(nameSpace,uri)
  }

  def setObject(obj: m.IRI) {
    send(Triple(subject,verb,obj))
  }

  def prefixes = prefixs.toMap

  var verb: m.IRI = _
  def setVerb(rel: m.IRI) {
    verb=rel
  }

  var subject: m.Node = _
  def setSubject(subj: m.IRI) {
      subject = subj
  }

  def setSubject(pname: PName) {
     subject = m.IRI(prefixes.get(pname.prefix).get + pname.name)
  }
}

case class PName(prefix: String, name: String)