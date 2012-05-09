package org.w3.banana.simple

import org.w3.banana._

object SimpleGraphUnion extends GraphUnion[SimpleRDF] {
  
  def union(left: SimpleRDF#Graph, right: SimpleRDF#Graph): SimpleRDF#Graph = left ++ right
  
}