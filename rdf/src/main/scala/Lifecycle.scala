package org.w3.banana

/**
 * A typeclass for stores that requires to be started or stopped
 */
trait Lifecycle[Rdf <: RDF, A] {

  /**
   * To the graph at `uri`, appends the content of `graph`. If there was
   * no previous graph, this would create it.
   */
  def start(a: A): Unit

  /**
   * To the graph at `uri`, removes the matching triples from `delete`,
   * then adds the ones in `insert`.
   */
  def stop(a: A): Unit

  val lifecycleSyntax = new syntax.LifecycleSyntax[Rdf, A]

}

object Lifecycle {

  implicit def defaultLifecycle[Rdf <: RDF, A]: Lifecycle[Rdf, A] = new Lifecycle[Rdf, A] {
    def start(a: A): Unit = ()
    def stop(a: A): Unit = ()
  }

}
