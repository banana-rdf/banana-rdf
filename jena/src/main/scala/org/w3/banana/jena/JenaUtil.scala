package org.w3.banana.jena

import com.hp.hpl.jena.graph.{ Factory, Node => JenaNode }
import com.hp.hpl.jena.rdf.model.{ Literal => JenaLiteral, _ }
import org.w3.banana._

class JenaUtil(implicit jenaOps: RDFOps[Jena]) {

  import jenaOps._

  val toNodeVisitor: RDFVisitor = new RDFVisitor {
    def visitBlank(r: Resource, id: AnonId) = makeBNodeLabel(id.getLabelString)

    def visitLiteral(l: JenaLiteral) = l.asNode.asInstanceOf[Jena#Literal]

    def visitURI(r: Resource, uri: String) = makeUri(uri)
  }

  def toNode(rdfNode: RDFNode): Jena#Node = rdfNode.visitWith(toNodeVisitor).asInstanceOf[Jena#Node]

  // usefull when you want to dump a graph for debugging :-)
  def dump[Rdf <: RDF](graph: Rdf#Graph)(implicit ops: RDFOps[Rdf]): Unit = {
    val mToJena = new RDFTransformer[Rdf, Jena](ops, jenaOps)
    val jenaGraph = mToJena.transform(graph)
    println(JenaRDFWriter.turtleWriter.asString(jenaGraph, ""))
  }

  def copy(graph: Jena#Graph): Jena#Graph = {
    val g = Factory.createDefaultGraph
    val it = graph.find(JenaNode.ANY, JenaNode.ANY, JenaNode.ANY)
    while (it.hasNext) { g.add(it.next()) }
    g
  }

}
