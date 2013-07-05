package org.w3.banana.ldp

import org.w3.banana._
import scala.concurrent._
import play.api.libs.iteratee._
import scala.util.{Failure, Success, Try}
import scala.Error

/** A resource that can be found with its URI, and is linked to other
  * resources through links as URIs
  * 
  * @tparam Rdf an RDF implementation
  * @tparam LR the kind of resources that can be linked
  */
trait LinkedResource[Rdf <: RDF, LR] {

  /** retrieves a resource based on its URI */
  def ~(uri: Rdf#URI): Enumerator[LR]

  /**
   * todo: could the fetchCond be better be done by an Enumeratee?
   *   ( eg if one could have an enumeratee that could transform an enumerator, by fetching the remote resource?
   *     but then one would want order not to be important !)
   *
   * @param lr
   * @param predicate
   * @param fetchCond
   * @return
   */
  def ~> (lr: LR, predicate: Rdf#URI)(fetchCond: PointedGraph[Rdf] => Boolean): Enumerator[LR]

  /** follow the predicate in reverse order  */
  def <~ (lr: LR, predicate: Rdf#URI)(fetchCond: PointedGraph[Rdf] => Boolean): Enumerator[LR]

}

/**
 * A resource with meta data
 * @tparam Rdf
 * @tparam LR
 */
trait LinkedMeta[Rdf <: RDF, LR] {

  /** follow the headers */
  def ≈>(lr: LR, predicate: Rdf#URI): Enumerator[LR]
}

trait LinkedWebResource[Rdf <: RDF, LR] extends LinkedResource[Rdf,LR] with LinkedMeta[Rdf,LR]


class WebResource[Rdf <:RDF](val rww: RWW[Rdf])(implicit ops: RDFOps[Rdf], ec: ExecutionContext)
  extends LinkedResource[Rdf,LinkedDataResource[Rdf]] {
  import LDPCommand._
  import ops._
  import diesel._
  import syntax._

  /** retrieves a resource based on its URI */
  def ~(uri: Rdf#URI): Enumerator[LinkedDataResource[Rdf]] = {
    val futureLDR: Future[LinkedDataResource[Rdf]] = rww.execute{
      //todo: this code could be moved somewhere else see: Command.GET
      val docUri = uri.fragmentLess
      getLDPR(docUri).map{graph=>
        val pointed = PointedGraph(uri, graph)
        LinkedDataResource(docUri, pointed)
      }
    }
    new Enumerator[LinkedDataResource[Rdf]] {
      def apply[A](i: Iteratee[LinkedDataResource[Rdf], A]): Future[Iteratee[LinkedDataResource[Rdf], A]] =
        futureLDR.flatMap { ldr =>
        i.feed(Input.El(ldr))
      }
    }
  }


  def ~>(lr: LinkedDataResource[Rdf], predicate: Rdf#URI)(fetchCond: PointedGraph[Rdf]=>Boolean=_=>true): Enumerator[LinkedDataResource[Rdf]] = {
    val res = lr.resource/predicate
    follow(lr.location, res, fetchCond )
  }

  //todo: add ~>(lr, predicate, condition: LR=>Where) Where can be Local, Remote, Union ( or binary ? )
  //I wonder if a LinkedDataResource is not a special case of a more general TrackerResource(Set[URI],PointedGraph)
  // which would keep track of where data came from.
  // So that ldr1 union ldr2 = a TrackerResource( Set(ldr1,ldr2), pointedGraph )

  /** follow the predicate in reverse order  */
  def <~(lr: LinkedDataResource[Rdf], predicate: Rdf#URI)(fetchCond: PointedGraph[Rdf]=>Boolean=_=>true) = {
    val res = lr.resource/-predicate
    follow(lr.location,  res, fetchCond)
  }


  /**
   * Transform the pointed Graphs to LinkedDataResources. If a graph is remote jump to the remote graph
   * definition.
   *
   * @param local the URI for local resources, others are remote and create a graph jump
   * @param res pointed Graphs to follow
   * @return An Enumerator that will feed the results to an Iteratee
   */
  protected def follow(local: Rdf#URI,res: PointedGraphs[Rdf], fetchCond: PointedGraph[Rdf]=>Boolean): Enumerator[LinkedDataResource[Rdf]] = {
    val local_remote = res.groupBy {
      pg =>
        if (!fetchCond(pg)) "local"
        else foldNode(pg.pointer)(
          uri => if (uri.fragmentLess == local)  "local" else "remote",
          bnode => "local",
          lit => "local"
        )
    }
    val localEnum = Enumerator(local_remote.get("local").getOrElse(Iterable()).toSeq.map {
      pg => LinkedDataResource(local, pg)
    }: _*)

    /*
     * see discussion http://stackoverflow.com/questions/15543261/transforming-a-seqfuturex-into-an-enumeratorx
     * In order to avoid the whole failing if only one of the Future[LDR]s fails, the failures of the sequence of Futures
     * have to be made visible by a Try. Then the results can be collected by the flt partial function.
     * @param seqFutureLdr: the Sequence of Futures where Failed Futures are mapped to try
     * @param flt: filter out all elements that don't succeed with the partial function
     */
    def toEnumerator[LDR](seqFutureLdr: Seq[Future[Try[LDR]]])(flt: PartialFunction[Try[LDR],LDR]) = new Enumerator[LDR] {
      def apply[A](i: Iteratee[LDR, A]): Future[Iteratee[LDR, A]] = {
        Future.sequence(seqFutureLdr).flatMap { seqLdrs: Seq[Try[LDR]] =>
          seqLdrs.collect(flt).foldLeft(Future.successful(i)) {
            case (i, ldr) => i.flatMap(_.feed(Input.El(ldr)))
          }
        }
      }
    }

    local_remote.get("remote").map { remote =>
      val remoteLdrs = remote.map { pg =>
        val pgUri = pg.pointer.asInstanceOf[Rdf#URI]
        val pgDoc = pgUri.fragmentLess
        //todo: the following code does not take redirects into account
        //todo: we need a GET that returns a LinkedDataResource, that knows how to follow redirects
        rww.execute(getLDPR(pgDoc)).map {
          g => LinkedDataResource(pgDoc, PointedGraph(pgUri, g))
        }
      }
      //move failures into a Try, so that the toEnumerator method does not fail if one future fails
      val nonFailing: Iterable[Future[Try[LinkedDataResource[Rdf]]]] = for {
        futureLdr <- remoteLdrs
      } yield {
        val f: Future[Try[LinkedDataResource[Rdf]]] = futureLdr.map(Success(_))
        f recover { case e => Failure(e) }
      }

      val rem = toEnumerator(nonFailing.toSeq) { case Success(ldr) => ldr}
      localEnum andThen rem

    } getOrElse (localEnum)
  }

}




/** A [[org.w3.banana.LinkedDataResource]] is obviously a [[org.w3.banana.ldp.LinkedResource]] */
class LDRLinkedResource[Rdf <: RDF]()(implicit ops: RDFOps[Rdf]) extends LinkedResource[Rdf, LinkedDataResource[Rdf]] {

  def ~(uri: Rdf#URI): Enumerator[LinkedDataResource[Rdf]] = ???

  def ~> (lr: LinkedDataResource[Rdf], predicate: Rdf#URI)(cond: PointedGraph[Rdf]=>Boolean): Enumerator[LinkedDataResource[Rdf]] = ???

  /** follow the predicate in reverse order  */
  def <~(lr: LinkedDataResource[Rdf], predicate: Rdf#URI)(fetchCond: PointedGraph[Rdf] => Boolean) = ???
}

/** Resources within a same RDF graph are linked together */
//class PointedGraphLinkedResource[Rdf <: RDF](pg: PointedGraph[Rdf])(implicit ops: RDFOps[Rdf]) extends LinkedResource[Rdf, PointedGraph[Rdf]] {
//
//  def ~(uri: Rdf#URI): Future[PointedGraph[Rdf]] =
//    Future.successful(PointedGraph(uri, pg.graph))
//
//  def ~> (lr: PointedGraph[Rdf], predicate: Rdf#URI): Enumerator[PointedGraph[Rdf]] = {
//    val nodes = ops.getObjects(pg.graph, pg.pointer, predicate)
//    nodes map { node => Future.successful(PointedGraph(node, pg.graph)) }
//  }
//
//}


