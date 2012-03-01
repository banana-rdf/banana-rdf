/*
* Copyright (c) 2012 Henry Story
* under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
*/
package org.w3.rdf.n3

import collection.mutable
import org.w3.rdf._
import java.net.{URISyntaxException, URI}
import org.apache.abdera.i18n.iri.{IRISyntaxException, IRI => aIRI}

//perhaps we should use this as our IRI?

/**
 * An agent that collects triples as they are built up and places them in a
 * structure which an other process can consume.
 * for a discussion see: https://bitbucket.org/pchiusano/nomo/issue/3/partial-results
 *
 * This class nearly needs to be functionalized and perhaps even rendered stateless.
 * But that is better done once one sees exactly what is needed.
 *
 * For the moment there is a bit of security, and the code will throw exceptions at runtime
 * if something is done wrong. It should not be able to do it though.
 */
case class Listener[Rdf <: RDF](val ops: RDFOperations[Rdf], val base: Option[URI]=None) {

  import ops._

  //we imagine that we are sending these elements to an agent.
  // todo: replace with more appropriate structure
  val queue: mutable.Queue[Rdf#Triple] = new mutable.Queue[Rdf#Triple]()
  def sendTriple(subj: Node, rel: IRI, obj: Node) = queue.enqueue(Triple(subj,rel,obj))
  def sendTriple(t: Triple) = queue.enqueue(t)


  trait Mem {
    /**
     * the subject of the memory, that which previous relations point to.
     */
    def subj: Node
    /*
     * send a triple made from the information in memory to the listening agent
     * @pram obj: the object of the relation
     */
    def send(obj: Node): Unit

    //clean up
    def end: Unit
  }

  /**
   * This can somewhat be thought of as a function constructed with a subject, and
   * when applied a relation creates a function that takes an object to return
   * a triple.
   * here the relation is alterable, and is change over time to reduce object creation
   * (since this can only be called serially)
   * @param subj
   */
  class PairMem(val subj: Rdf#Node) extends Mem {
    //rel is the relation to keep track of, or for lists the first element of the list
    var rel : IRI = _
    def send(obj: Node) { sendTriple(subj,rel,obj) }

    def end {}
  }

  /**
   *
   */
  class ListMem extends Mem {
    //the first element of a list (set to rdfNil, for cases where the list is empty)
    //we don't want to remember all the elements of what may be a huge (even infinite!) list. But we
    // need the first element as we then may want to set a relation to the first element of the list as in
    //    a rel ( b c ) .
    // We cannot set this relation initially because we cannot distinguish the previous relation and
    //    a rel () .
    // which is equivalent to
    //    a rel rdf:nil .
   private var first: Node = rdfNil

   /** the subject of the whole list. Is only known when the list is shown to be either empty or
    * when the first element has been found */
   def subj = first

    /**
     * The previous subject so that one can construct
     *     previousSubject rdf:next newSubject .
     */
   private var previousSubject: Node = first

   //subject of individual triples constructed in the list
   private def newSubject(): Node = {
      val subj: Node = BNode().asInstanceOf[Node]
      if (first == rdfNil) first = subj
      previousSubject = subj
     subj
    }

   def send(obj: Node) {
     val previous = previousSubject
     val subj = newSubject()
     if (previous != rdfNil) sendTriple(previous,rdfRest,subj) //else this is the first element of the list
     sendTriple(subj,rdfFirst,obj)
   }

    def end {
      if (first!=rdfNil) sendTriple(previousSubject,rdfRest,rdfNil)
    }


  }

  /**
   * the builder remembers subject, predicate pairs, and
   * stacks them so that one can push deeper into a tree with notations
   * like
   *    subj rel [ rel obj ] .
   * or
   *    subj rel ( a b c ) .
   *
   * Lists require a different type of memory, hence we have two subclasses of mem
   */
  val context = mutable.Stack[Mem]()

  /* exit a list, a [...] or a statement */
  def pop = {
    val previous = context.pop
    previous.end
    previous.subj
  }

  /**
   * these three methods can only be called when not in list mode, ie when the
   * top of the stack is a PairMem
   */

  /**
   * create a new subject
   * @param subj
   */
  def pushSubject(subj: Node) {
    context.push(new PairMem(subj))
  }

  def pushList {
    context.push(new ListMem())
  }


  /**
   * @param rel the relation
   */
  def setVerb(rel: Rdf#IRI) {
    val pm = context.head.asInstanceOf[PairMem]
    pm.rel=rel
  }

  def setObject(obj: Node) {
    context.top.send(obj)
  }

  /**
   * prefix related
   */

  private val prefixs = new mutable.HashMap[String, Rdf#IRI]
  def prefixes = prefixs.toMap

  def resolve(pname: PName): Option[Rdf#IRI] = {
    prefixs.get(pname.prefix).map{ case IRI(pre)=> IRI(pre + pname.name)}
  }

  var currentBase = base.map(u=>new aIRI(u))
  @throws(classOf[IRISyntaxException])
  def alterBase(newbase: IRI) {
    currentBase = newbase match {
      case IRI(i) => Some(new aIRI(i))
    }
  }

  def addPrefix(name: String, value: Rdf#IRI) {
    prefixs.put(name, value)
  }

  @throws(classOf[IRISyntaxException])
  def resolve(iriStr: String): Rdf#IRI = {
     val iri = currentBase.map( _.resolve(iriStr)).getOrElse(new aIRI(iriStr))
     IRI(iri.toString)
  }


}

case class PName(prefix: String, name: String)