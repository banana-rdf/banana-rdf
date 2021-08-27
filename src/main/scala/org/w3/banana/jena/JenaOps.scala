package org.w3.banana.jena

import org.apache.jena.graph.{Factory, NodeFactory, Node_URI}
import org.w3.banana.RDFOps
import org.w3.banana.jena.*

class JenaOps extends RDFOps(Jena): // with JenaMGraphOps with DefaultURIOps[Jena]*

	// graph

  val emptyGraph: rdf.Graph = Factory.createDefaultGraph

  def makeUri(iriStr: String): rdf.URI = { NodeFactory.createURI(iriStr).asInstanceOf[Node_URI] }

  def fromUri(node: rdf.URI): String =
    if (node.isURI)
      node.getURI
    else
      throw new RuntimeException("fromUri: " + node.toString() + " must be a URI")


