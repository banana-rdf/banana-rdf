package org.w3.banana.syntax

import org.w3.banana._

trait MGraphSyntax[Rdf <: RDF] { self: RDFSyntax[Rdf] =>

  implicit def mgraphW(mgraph: Rdf#MGraph) = new MGraphW[Rdf](mgraph)

}

class MGraphW[Rdf <: RDF](val mgraph: Rdf#MGraph) extends AnyVal {

  def +=(triple: Rdf#Triple)(implicit ops: RDFOps[Rdf]): mgraph.type =
    ops.addTriple(mgraph, triple)

  def ++=(triples: TraversableOnce[Rdf#Triple])(implicit ops: RDFOps[Rdf]): mgraph.type =
    ops.addTriples(mgraph, triples)

  def -=(triple: Rdf#Triple)(implicit ops: RDFOps[Rdf]): mgraph.type =
    try ops.removeTriple(mgraph, triple) catch { case e: NoSuchElementException => mgraph }

  def --=(triples: TraversableOnce[Rdf#Triple])(implicit ops: RDFOps[Rdf]): mgraph.type =
    ops.removeTriples(mgraph, triples)

  def exists(triple: Rdf#Triple)(implicit ops: RDFOps[Rdf]): Boolean =
    ops.exists(mgraph, triple)

  def makeIGraph()(implicit ops: RDFOps[Rdf]): Rdf#Graph =
    ops.makeIGraph(mgraph)

  def size(implicit ops: RDFOps[Rdf]): Int = ops.sizeMGraph(mgraph)

}
