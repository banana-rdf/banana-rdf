package org.w3.banana.bigdata

import java.io.File

import com.bigdata.io.DirectBufferPool
import com.bigdata.journal.TemporaryStore
import com.bigdata.rdf.model.BigdataValue
import com.bigdata.rdf.sail.{BigdataSailRepositoryConnection, BigdataSail, BigdataSailRepository}
import com.bigdata.rdf.store.TempTripleStore
import com.bigdata.rwstore.sector.{MemoryManager, MemStore}
import org.openrdf.query.BindingSet
import org.w3.banana.{RDFOps, SparqlEngine}

import scala.util.Try


class TemporalBigdata(implicit val ops:RDFOps[Bigdata]) extends BigdataSparqlEngine{


  /*
 initiates embeded bigdata database
  */
  val sail: BigdataSail = {
    //val journal = File.createTempFile("sparql_graph_"+Math.random(),BigdataConfig.journal)
    //BigdataConfig.properties.setProperty(BigdataSail.Options.DEFAULT_FILE, journal.getAbsolutePath)
    //new BigdataSail(BigdataConfig.properties)
    val temp = new TempTripleStore(new TemporaryStore(), BigdataConfig.properties, null)
    new BigdataSail(temp)
  }

  val repo: BigdataSailRepository = {
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
    /*val write = store.rw(con,{
      graph.triples.foreach{
        case tr=>
          //con.add(f.createURI("hello:world"+Math.random()),f.createURI("rel:who?"+Math.random()),f.createLiteral("HELLO?"))
          con.add(tr.getSubject,tr.getPredicate,tr.getObject,tr.getContext)
        }
      }
    )
    write.map(r=>store)*/
    con.setAutoCommit(false)
    Try{
      graph.triples.foreach(t=> con.add(t))
      con.commit2()
      con.close()
      store
    }
  }



  /** Executes a Select query. */
  override def executeSelect(a: BigdataGraph, query: String, bindings: Map[String, BigdataValue]): Try[Vector[BindingSet]] = {
    val storeOpt =  this.withGraph(a)
    println(storeOpt)
    val store =  storeOpt.get
    import org.w3.banana.bigdata.extensions._
    val con = store.repo.getReadOnlyConnection()
    println("*******************************************************")
    val sts = con.getStatements(null,null,null,false,null).toList
    sts.foreach(st=>println(s"STATEMENT = $st"))
    con.close()
    val result = store.executeSelect(store.repo.getReadOnlyConnection,query,bindings)
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



