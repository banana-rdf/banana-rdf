package org.w3.banana.ldp

import org.w3.banana._
import scala.concurrent._

/** A resource that can be find with its URI, and is linked to other
  * resources through links as URIs
  * 
  * @tparam Rdf an RDF implementation
  * @tparam the kind of resources that can be linked
  */
trait LinkedResource[Rdf <: RDF, LR] {

  /** retrieves a resource based on its URI */
  def ~(uri: Rdf#URI): Future[LR]

  /** follow the  */
  def ~> (lr: LR, predicate: Rdf#URI): Iterable[Future[LR]]

}

/** A [[org.w3.banana.LinkedDataResource]] is obviously a [[org.w3.banana.ldp.LinkedResource]] */
class LDRLinkedResource[Rdf <: RDF]()(implicit ops: RDFOps[Rdf]) extends LinkedResource[Rdf, LinkedDataResource[Rdf]] {

  def ~(uri: Rdf#URI): Future[LinkedDataResource[Rdf]] = ???

  def ~> (lr: LinkedDataResource[Rdf], predicate: Rdf#URI): Iterable[Future[LinkedDataResource[Rdf]]] = ???
  

}

/** Resources within a same RDF graph are linked together */
class PointedGraphLinkedResource[Rdf <: RDF](pg: PointedGraph[Rdf])(implicit ops: RDFOps[Rdf]) extends LinkedResource[Rdf, PointedGraph[Rdf]] {

  def ~(uri: Rdf#URI): Future[PointedGraph[Rdf]] =
    Future.successful(PointedGraph(uri, pg.graph))

  def ~> (lr: PointedGraph[Rdf], predicate: Rdf#URI): Iterable[Future[PointedGraph[Rdf]]] = {
    val nodes = ops.getObjects(pg.graph, pg.pointer, predicate)
    nodes map { node => Future.successful(PointedGraph(node, pg.graph)) }
  }
  
}
