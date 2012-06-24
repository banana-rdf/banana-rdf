/*
* Copyright (c) 2012 Henry Story
* under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
*/
package org.w3.banana.n3

import collection.mutable
import org.w3.banana._
import java.net.{URISyntaxException, URI => jURI}
import org.apache.abdera.i18n.iri.{IRISyntaxException, IRI => aURI}

//perhaps we should use this as our URI?

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
case class Listener[Rdf <: RDF](ops: RDFOperations[Rdf], base: Option[jURI]=None) {

  import ops._

  private val rdf = RDFPrefix(ops)

  //we imagine that we are sending these elements to an agent.
  // todo: replace with more appropriate structure
  val queue: mutable.Queue[Rdf#Triple] = new mutable.Queue[Rdf#Triple]()
  def sendTriple(subj: Rdf#Node, rel: Rdf#URI, obj: Rdf#Node) = queue.enqueue(makeTriple(subj,rel,obj))
  def sendTriple(t: Rdf#Triple) = queue.enqueue(t)


  trait Mem {
    /**
     * the subject of the memory, that which previous relations point to.
     */
    def subj: Rdf#Node
    /*
     * send a triple made from the information in memory to the listening agent
     * @pram obj: the object of the relation
     */
    def send(obj: Rdf#Node): Unit

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
    var rel : Rdf#URI = _
    def send(obj: Rdf#Node) { sendTriple(subj,rel,obj) }

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
   private var first: Rdf#Node = rdf.nil

   /** the subject of the whole list. Is only known when the list is shown to be either empty or
    * when the first element has been found */
   def subj = rdf.first

    /**
     * The previous subject so that one can construct
     *     previousSubject rdf:next newSubject .
     */
   private var previousSubject: Rdf#Node = rdf.first

   //subject of individual triples constructed in the list
   private def newSubject(): Rdf#Node = {
      val subj: Rdf#Node = makeBNode()
      if (first == rdf.nil) first = subj
      previousSubject = subj
     subj
    }

   def send(obj: Rdf#Node) {
     val previous = previousSubject
     val subj = newSubject()
     if (previous != rdf.nil) sendTriple(previous,rdf.rest,subj) //else this is the first element of the list
     sendTriple(subj,rdf.first,obj)
   }

    def end {
      if (first!=rdf.nil) sendTriple(previousSubject,rdf.rest,rdf.nil)
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
  def pushSubject(subj: Rdf#Node) {
    context.push(new PairMem(subj))
  }

  def pushList {
    context.push(new ListMem())
  }


  /**
   * @param rel the relation
   */
  def setVerb(rel: Rdf#URI) {
    val pm = context.head.asInstanceOf[PairMem]
    pm.rel=rel
  }

  def setObject(obj: Rdf#Node) {
    context.top.send(obj)
  }

  /**
   * prefix related
   */

  private val prefixs = new mutable.HashMap[String, Rdf#URI]
  def prefixes = prefixs.toMap

  def resolve(pname: PName): Option[Rdf#URI] = {
    prefixs.get(pname.prefix).map{ pre => makeUri(fromUri(pre) + pname.name)}
  }

  var currentBase = base.map(u=>new aURI(u))
  @throws(classOf[IRISyntaxException])
  def alterBase(newbase: Rdf#URI) {
    currentBase = Some(new aURI(fromUri(newbase)))
  }

  def addPrefix(name: String, value: Rdf#URI) {
    prefixs.put(name, value)
  }

  @throws(classOf[IRISyntaxException])
  def resolve(iriStr: String): Rdf#URI = {
     val iri = currentBase.map{ b=>
       if ("#" == iriStr) new aURI(b.toString+"#")
       else b.resolve(iriStr)
     }.getOrElse(new aURI(iriStr))
     makeUri(iri.toString)
  }


}

case class PName(prefix: String, name: String)
