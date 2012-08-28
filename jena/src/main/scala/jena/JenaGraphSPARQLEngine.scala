package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.sparql.core.DatasetGraph
import com.hp.hpl.jena.rdf.model.ModelFactory.createModelForGraph

object JenaGraphSPARQLEngine extends RDFGraphQuery[Jena] {

  def makeSPARQLEngine(graph: Jena#Graph): SPARQLEngine[Jena] = {
    val datasource = graph match {
      case dsg: DatasetGraph => DatasetFactory.create(dsg)
      case _ => DatasetFactory.create(createModelForGraph(graph.jenaGraph))
    }
    new JenaStore(datasource, false)
  }


}
