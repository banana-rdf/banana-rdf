package org.w3.banana.bigdata

import com.bigdata.rdf.model.BigdataValue
import com.bigdata.rdf.sail.{BigdataSail, BigdataSailRepository}
import org.openrdf.query.BindingSet
import org.w3.banana.{RDFOps, SparqlEngine}

import scala.util.Try


class TemporalBigdata(implicit val ops:RDFOps[Bigdata]) extends BigdataSparqlEngine{

  val repo: BigdataSailRepository = {
    val sail = new BigdataSail(BigdataConfig.inmemoryConfig)
    val repo = new BigdataSailRepository(sail)
    repo.initialize()
    repo
  }

}


class BigdataGraphSparqlEngine(implicit ops:RDFOps[Bigdata])
  extends  SparqlEngine[Bigdata, Try, Bigdata#Graph] with BigdataTransactor
{

  def withGraph(graph: Bigdata#Graph) = {
    val store = new TemporalBigdata
    val con = store.repo.getUnisolatedConnection //only one unisolated writer is possible
    graph.prefixes.find(p=>p.prefixName=="").foreach(p=>BigdataConfig.basePrefix=p.prefixIri)
    store.rw(con,{
        graph.triples.foreach{  case tr=>   con.add(tr)   }
        store
      }
    )
  }



  /** Executes a Select query. */
  override def executeSelect(
                              a: BigdataGraph,
                              query: String,
                              bindings: Map[String, BigdataValue]
                              ): Try[Vector[BindingSet]] =
  this.withGraph(a) flatMap {
    case store=>
      val result: Try[Vector[BindingSet]] = store.executeSelect(store.repo.getReadOnlyConnection,query,bindings)
      store.repo.shutDown()
      result
  }

  /** Executes a Construct query. */
  override def executeConstruct(a: BigdataGraph, query: String, bindings: Map[String, BigdataValue]): Try[BigdataGraph] = {
    val store = this.withGraph(a) get
    val result = store.executeConstruct(store.repo.getReadOnlyConnection,query,bindings)
    store.repo.shutDown()
    result
  }

  /** Executes a Ask query. */
  override def executeAsk(a: BigdataGraph, query: String, bindings: Map[String, BigdataValue]): Try[Boolean] = {
    val store = this.withGraph(a) get
    val result = store.executeAsk(store.repo.getReadOnlyConnection,query,bindings)
    store.repo.shutDown()
    result
  }
}

object BigdataGraphSparqlEngine {
  def apply() = new BigdataGraphSparqlEngine()
}



