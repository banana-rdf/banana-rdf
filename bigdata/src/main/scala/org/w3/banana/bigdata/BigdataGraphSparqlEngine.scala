package org.w3.banana.bigdata

import java.io.File

import com.bigdata.rdf.model.BigdataValue
import com.bigdata.rdf.sail.{BigdataSail, BigdataSailRepository}
import org.openrdf.query.BindingSet
import org.w3.banana.{RDFOps, SparqlEngine}

import scala.util.Try

//THERE IS NOT INMEMORY VERSION FOR BIGDATA RIGHT NOW, I do not know what I should do
/*
class BigdataGraphSparqlEngine(implicit ops:RDFOps[Bigdata], config:BigdataConfig[Bigdata])
  extends  SparqlEngine[Bigdata, Try, Bigdata#Graph] with BigdataTransactor
{

  /*
 initiates embeded bigdata database
  */
  val sail: BigdataSail = {
    val journal = new File(config.journal)
    if(!journal.exists())journal.createNewFile()
    config.properties.setProperty(BigdataSail.Options.DEFAULT_FILE, journal.getAbsolutePath)

    new BigdataSail(config.properties)
  }

  val repo: BigdataSailRepository = {
    val repo = new BigdataSailRepository(sail)
    repo.initialize()
    repo
  }

  /** Executes a Select query. */
  override def executeSelect(a: BigdataGraph, query: String, bindings: Map[String, BigdataValue]): Try[Vector[BindingSet]] = {
    val con = repo.getUnisolatedConnection
    rw(repo.getConnection,{
     a.triples.foreach(t=>con.add(t))
    })
  }

  /** Executes a Construct query. */
  override def executeConstruct(a: BigdataGraph, query: String, bindings: Map[String, BigdataValue]): Try[BigdataGraph] = ???

  /** Executes a Ask query. */
  override def executeAsk(a: BigdataGraph, query: String, bindings: Map[String, BigdataValue]): Try[Boolean] = ???
}

object BigdataGraphSparqlEngine {
  def apply() = new BigdataGraphSparqlEngine()
}
*/


