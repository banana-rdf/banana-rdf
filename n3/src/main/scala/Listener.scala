/*
* Copyright (c) 2012 Henry Story
* under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
*/
package org.w3.rdf.n3

import collection.mutable
import org.w3.rdf._

/**
 * An agent that collects triples as they are built up and places them in a
 * structure which an other process can consume.
 * for a discussion see: https://bitbucket.org/pchiusano/nomo/issue/3/partial-results
 */
case class Listener[Rdf <: RDF](val ops: RDFOperations[Rdf]) {

  import ops._

  //we imagine that we are sending these elements to an agent.
  // todo: replace with more appropriate structure
  val queue: mutable.Queue[Rdf#Triple] = new mutable.Queue[Rdf#Triple]()

  class Pair(val subj: Rdf#Node) {
    var rel : Rdf#IRI = _
  }

  /**
   * the builder remembers subject, predicate pairs, and
   * stacks them so that one can push deeper into a tree with notations
   * like
   *    subj rel [ rel obj ] .
   */
  val context = mutable.Stack[Pair]()

  // Perhaps it could send the triple to an agent here
  def send(a: Rdf#Triple) = queue.enqueue(a)

  def addPrefix(name: String, value: Rdf#IRI) {
    prefixs.put(name, value)
  }

  def pop = context.pop

  def pushObj(obj: Rdf#Node) {
    setObject(obj)
    context.push(new Pair(obj))
  }

  def setObject(obj: Rdf#Node) {
    val pair = context.top
    send(Triple(pair.subj, pair.rel, obj))
  }

  def resolve(pname: PName): Option[Rdf#IRI] = {
    prefixs.get(pname.prefix).map{ case IRI(pre)=> IRI(pre + pname.name)}
  }

  def setVerb(rel: Rdf#IRI) {
      context.top.rel=rel
  }

  def setSubject(subj: Rdf#Node) {
      context.push(new Pair(subj))
  }

  private val prefixs = new mutable.HashMap[String, Rdf#IRI]
  def prefixes = prefixs.toMap

}

case class PName(prefix: String, name: String)