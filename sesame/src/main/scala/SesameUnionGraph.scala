package org.w3.rdf.sesame

import org.w3.rdf._
import org.openrdf.model.impl.GraphImpl
import SesameOperations.Graph.toIterable

object SesameGraphUnion extends GraphUnion[Sesame] {
  
  def union(left: Sesame#Graph, right: Sesame#Graph): Sesame#Graph = {
    val graph = new GraphImpl
    toIterable(left) foreach { t => graph add t }
    toIterable(right) foreach { t => graph add t }
    graph
  }
  
}