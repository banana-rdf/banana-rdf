package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.graph.Factory
import JenaOperations.Graph.toIterable

object JenaGraphUnion extends GraphUnion[Jena] {
  
  def union(left: Jena#Graph, right: Jena#Graph): Jena#Graph = {
    if (left.isEmpty)
      right
    else if (right.isEmpty)
      left
    else {
      val graph = Factory.createDefaultGraph
      toIterable(left) foreach { t => graph add t }
      toIterable(right) foreach { t => graph add t }
      graph
    }
  }
  
}
