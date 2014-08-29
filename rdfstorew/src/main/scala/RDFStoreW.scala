package org.w3.banana.rdfstorew

import scala.concurrent._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.global
import org.w3.banana.{ RDFStore => RDFStoreInterface }
import org.w3.banana._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

import scala.language.postfixOps
import scala.util.Try
import scalaz.Free

class RDFStoreW(store: js.Dynamic) extends RDFStoreInterface[RDFStore] {

  def RDF_API = store.selectDynamic("rdf").selectDynamic("api")

  def RDF = store.selectDynamic("rdf")

  def executeQuery(sparql: String): Future[Any] = {
    val promise = Promise[Any]

    store.applyDynamic("execute")(sparql, { (success: Boolean, res: js.Any) =>
      if (success) {
        promise.success(res)
      } else {
        promise.failure(new Exception("Error running query: " + res))
      }
    })

    promise.future
  }

  def load(mediaType: String, data: String, graph: String = null): Future[Boolean] = {
    val promise = Promise[Boolean]
    val cb = {
      (success: Boolean, res: Any) =>
        if (success) {
          promise.success(true)
        } else {
          promise.failure(new Exception("Error loading data into the store: " + res))
        }
    }

    if (graph == null) {
      store.applyDynamic("load")(mediaType, data, cb)
    } else {
      store.applyDynamic("load")(mediaType, data, graph, cb)
    }

    promise.future
  }

  def clean(graph: String = null): Future[Boolean] = {
    val promise = Promise[Boolean]
    val cb = {
      (success: Boolean, res: Any) =>
        if (success) {
          promise.success(true)
        } else {
          promise.failure(new Exception("Error cleaning graph from the store store: " + res))
        }
    }

    if (graph == null) {
      store.applyDynamic("clear")(cb)
    } else {
      store.applyDynamic("clear")(graph, cb)
    }

    promise.future
  }

  def insert(triples: RDFStoreGraph, graph: String = null): Future[RDFStoreW] = {
    val promise = Promise[RDFStoreW]
    val cb = {
      (success: Boolean, res: Any) =>
        if (success) {
          promise.success(this)
        } else {
          promise.failure(new Exception("Error inserting triples into the store: " + res))
        }
    }

    if (graph == null) {
      store.applyDynamic("insert")(triples.graph, cb)
    } else {
      store.applyDynamic("insert")(triples.graph, graph, cb)
    }

    promise.future
  }

  def delete(triples: RDFStoreGraph, graph: String = null): Future[Boolean] = {
    val promise = Promise[Boolean]
    val cb = {
      (success: Boolean, res: Any) =>
        if (success) {
          promise.success(true)
        } else {
          promise.failure(new Exception("Error deleting triples into the store: " + res))
        }
    }

    if (graph == null) {
      store.applyDynamic("delete")(triples.graph, cb)
    } else {
      store.applyDynamic("delete")(triples.graph, graph, cb)
    }

    promise.future
  }

  def bindQuery(query: String, bindings: Map[String, RDFStoreRDFNode]): String = {
    var tmp = query
    for ((name, node) <- bindings) {
      tmp = tmp.replaceAll("?" + name, node.jsNode.toNT().asInstanceOf[js.String])
    }
    tmp
  }

  def toGraph(base: String): Future[RDFStoreGraph] = {
    val promise = Promise[RDFStoreGraph]
    val cb = {
      (success: Boolean, res: js.Dynamic) =>
        if (success) {
          promise.success(new RDFStoreGraph(res))
        } else {
          promise.failure(new Exception("Error exporting data as a RDF graph"))
        }
    }
    store.applyDynamic("graph")(base, cb)
    promise.future
  }

  override def shutdown(): Unit = Unit

  import org.w3.banana.rdfstorew.RDFStore.Ops._

  override def execute[A](script: Free[({ type l[+x] = Command[RDFStore, x] })#l, A]): Future[A] = {
    val res = script.resume fold (
      {
        case Create(uri, a) => {
          val cleaned: Future[A] = clean(uri.valueOf) flatMap {
            _ =>
              load("text/n3", "", uri.valueOf)
          } flatMap {
            _ => execute(a)
          }
          cleaned
        }
        case Delete(uri, a) => {
          clean(uri.valueOf)
        }
        case Get(uri, k) => {
          toGraph(uri.valueOf)
        }
        case Append(uri, triples, a) => {
          val g = emptyGraph
          for (triple <- triples) g.add(triple)

          insert(g, uri.valueOf)
        }
        case Remove(uri, tripleMatches, a) => {
          val g = emptyGraph
          for (triple <- tripleMatches) {
            triple match {
              case (PlainNode(s), PlainNode(p), PlainNode(o)) => {
                val triple = makeTriple(s, p.asInstanceOf[RDFStoreNamedNode], o)
                g.add(triple)
              }
              case _ => // ignore
            }
          }

          val deleted = delete(g, uri.valueOf) flatMap {
            _ => execute(a)
          }

          deleted
        }
        case Select(query, bindings, k) => {
          executeQuery(bindQuery(query, bindings)) map {
            solutions =>
              solutions.asInstanceOf[js.Array[js.Dynamic]].map[SPARQLSolutionTuple] {
                (o: js.Dynamic) => new SPARQLSolutionTuple(o.asInstanceOf[js.Dictionary[js.Any]])
              }.toArray
          }
        }
        case Construct(query, bindings, k) => {
          executeQuery(bindQuery(query, bindings)) map {
            g =>
              {
                new RDFStoreGraph(g.asInstanceOf[js.Dynamic])
              }
          }
        }
        case Ask(query, bindings, k) => {
          executeQuery(bindQuery(query, bindings)) map {
            b =>
              {
                b.asInstanceOf[Boolean]
              }
          }
        }
        case org.w3.banana.Update(query, bindings, k) => {
          executeQuery(bindQuery(query, bindings)) map {
            b =>
              {
                b
              }
          }
        }
      },
      a => a
    )

    res.asInstanceOf[Future[A]]
  }

}

object RDFStoreW {

  val rdf = apply(Map()).RDF

  val rdf_api = apply(Map()).RDF_API

  def apply(options: Map[String, Any]): RDFStoreW = {
    val dic = options.foldLeft[js.Dictionary[Any]](js.Dictionary())({
      case (acc, (key, value)) =>
        acc.update(key, value); acc
    })

    val promise = Promise[RDFStoreW]

    // hack for Rhino/browser execution
    var rdfstore = if (global.window != null) {
      global.window.rdfstore
    } else {
      global.rdfstore
    }
    rdfstore.applyDynamic("create")(dic, (store: js.Dynamic) => promise.success(new RDFStoreW(store)))

    // always succeeds
    promise.future.value.get.get
  }
}