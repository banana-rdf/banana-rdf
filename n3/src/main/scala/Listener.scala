package org.w3.rdf.n3

import collection.{immutable, mutable}
import org.w3.rdf.RDFModule

case class Listener[M <: RDFModule](val m: M) {
  private val prefixs = new mutable.HashMap[String, m.IRI]
  val queue: mutable.Queue[m.Triple] = new mutable.Queue[m.Triple]()

  def send(a: Any) = queue.enqueue(a.asInstanceOf[m.Triple])

  def addPrefix(nameSpace: String, uri: Any) {
    prefixs.put(nameSpace,uri.asInstanceOf[m.IRI])
  }

  def setObject(obj: Any) {
    send(m.Triple(subject,verb,obj.asInstanceOf[m.Node]))
  }

  def prefixes = prefixs.toMap

  var verb: m.IRI = _
  def setVerb(rel: Any) {
    verb=rel.asInstanceOf[m.IRI]
  }

  var subject: m.Node = _
  def setSubject(subj: Any) {
    subject = subj match {
      case pname: PName => m.IRI(prefixes.get(pname.prefix).get + pname.name)
      case iri: m.IRI => iri
    }
  }


}

case class PName(prefix: String, name: String)