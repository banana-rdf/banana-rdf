package org.w3.banana.ldp

import org.w3.banana._
import scala.concurrent._
import play.api.libs.iteratee.Enumerator
import scala.util.{Failure, Success, Try}

/** A resource that can be found with its URI, and is linked to other
  * resources through links as URIs
  * 
  * @tparam Rdf an RDF implementation
  * @tparam LR the kind of resources that can be linked
  */
trait LinkedResource[Rdf <: RDF, LR] {

  /** retrieves a resource based on its URI */
  def ~(uri: Rdf#URI): Future[LR]

  /** follow the  */
  def ~> (lr: LR, predicate: Rdf#URI): Enumerator[LR]

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


class WebResource[Rdf <:RDF]()(implicit rww: RWW[Rdf], ops: RDFOps[Rdf], ec: ExecutionContext)
  extends LinkedResource[Rdf,LinkedDataResource[Rdf]] {
  import LDPCommand._
  import ops._
  import diesel._
  import syntax._

  /** retrieves a resource based on its URI */
  def ~(uri: Rdf#URI): Future[LinkedDataResource[Rdf]] = rww.execute{
    //todo: this code could be moved somewhere else see: Command.GET
    val docUri = uri.fragmentLess
    getLDPR(docUri).map{graph=>
      val pointed = PointedGraph(uri, graph)
      LinkedDataResource(docUri, pointed)
    }
  }

  /** follow the  */
  def ~>(lr: LinkedDataResource[Rdf], predicate: Rdf#URI): Enumerator[LinkedDataResource[Rdf]] = {
    val res = lr.resource/predicate
    val local_remote = res.groupBy{pg =>
       foldNode(pg.pointer)(
         uri=>if (uri.fragmentLess == lr.location) "local" else "remote",
         bnode => "local",
         lit => "local"
       )
    }
    val localEnum = Enumerator(local_remote.get("local").getOrElse(Iterable()).toSeq.map {pg => LinkedDataResource(lr.location,pg)}: _*)

    local_remote.get("remote").map { remote =>
      val remoteLdrs = remote.map { pg =>
        val pgUri = pg.pointer.asInstanceOf[Rdf#URI]
        //todo: the following code does not take redirects into account
        //todo: we need a GET that returns a LinkedDataResource, that knows how to follow redirects
        rww.execute(getLDPR(pgUri)).map {g => LinkedDataResource(pgUri.fragmentLess,PointedGraph(pgUri,g))}
      }

      val rem = Enumerator.unfoldM(remoteLdrs.toSeq){s =>
        if (s.isEmpty) {
          Future(None)
        } else {
          FutureUtil.select(s).map {
            case (t, seqFuture) => t.toOption.map {
              ldr => (seqFuture, ldr)
            }
          }
        }
      }
      localEnum andThen rem
    } getOrElse(localEnum)
  }
}

object FutureUtil {

  /**
   * "Select" off the first future to be satisfied.  Return this as a
   * result, with the remainder of the Futures as a sequence.
   *
   * @param fs a scala.collection.Seq
   */
  def select[A](fs: Seq[Future[A]])(implicit ec: ExecutionContext): Future[(Try[A], Seq[Future[A]])] = {
    //todo: this just seems very inefficient. There must be a way to improve this
    @scala.annotation.tailrec
    def stripe(p: Promise[(Try[A], Seq[Future[A]])],
               heads: Seq[Future[A]],
               elem: Future[A],
               tail: Seq[Future[A]]): Future[(Try[A], Seq[Future[A]])] = {
      elem onComplete { res => if (!p.isCompleted) p.trySuccess((res, heads ++ tail)) }
      if (tail.isEmpty) p.future
      else stripe(p, heads :+ elem, tail.head, tail.tail)
    }

    if (fs.isEmpty) Future.failed(new IllegalArgumentException("empty future list!"))
    else stripe(Promise(), fs.genericBuilder[Future[A]].result, fs.head, fs.tail)
  }

}



/** A [[org.w3.banana.LinkedDataResource]] is obviously a [[org.w3.banana.ldp.LinkedResource]] */
class LDRLinkedResource[Rdf <: RDF]()(implicit ops: RDFOps[Rdf]) extends LinkedResource[Rdf, LinkedDataResource[Rdf]] {

  def ~(uri: Rdf#URI): Future[LinkedDataResource[Rdf]] = ???

  def ~> (lr: LinkedDataResource[Rdf], predicate: Rdf#URI): Enumerator[LinkedDataResource[Rdf]] = ???

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


