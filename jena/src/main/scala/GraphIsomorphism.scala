package org.w3.rdf.jena

import org.w3.rdf

object GraphIsomorphism extends rdf.GraphIsomorphism[JenaModule.type](JenaModule) {
  
  def isIsomorphicWith(g1: m.Graph, g2: m.Graph): Boolean =
    g1.jenaGraph isIsomorphicWith g2.jenaGraph
  
  def diff(g1: m.Graph, g2: m.Graph): m.Graph = {
    import com.hp.hpl.jena.rdf.model.ModelFactory.createModelForGraph
    val m1 = createModelForGraph(g1.jenaGraph)
    val m2 = createModelForGraph(g2.jenaGraph)
    val diffModel = m1 difference m2
    m.Graph.fromJena(diffModel.getGraph)
  }
  
}