package org.w3.rdf.jena

import org.w3.rdf._
import com.hp.hpl.jena.graph.Factory
import JenaOperations.Graph.toIterable

object JenaGraphUnion extends GraphUnion[JenaDataType] {
  
  def union(left: JenaDataType#Graph, right: JenaDataType#Graph): JenaDataType#Graph = {
    val graph = Factory.createDefaultGraph
    toIterable(left) foreach { t => graph add t }
    toIterable(right) foreach { t => graph add t }
    graph
  }
  
}