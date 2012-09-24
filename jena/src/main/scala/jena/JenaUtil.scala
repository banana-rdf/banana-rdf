package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.rdf.model.{ Literal => JenaLiteral, _ }
import com.hp.hpl.jena.rdf.model.ResourceFactory._
import com.hp.hpl.jena.util.iterator._
import com.hp.hpl.jena.graph.{ Factory, Node => JenaNode }
import JenaOperations._

object JenaUtil {

  val toNodeVisitor: RDFVisitor = new RDFVisitor {
    def visitBlank(r: Resource, id: AnonId) = makeBNodeLabel(id.getLabelString)
    // go from the model's Node to the Graph one, then cast it (always safe here)
    def visitLiteral(l: JenaLiteral) = foldLiteral(l.asNode.asInstanceOf[Jena#Literal])(x => x, x => x)
    def visitURI(r: Resource, uri: String) = makeUri(uri)
  }

  def toNode(rdfNode: RDFNode): Jena#Node = rdfNode.visitWith(toNodeVisitor).asInstanceOf[Jena#Node]

  // usefull when you want to dump a graph for debugging :-)
  def dump[Rdf <: RDF](graph: Rdf#Graph)(implicit ops: RDFOps[Rdf]): Unit = {
    val mToJena = new RDFTransformer[Rdf, Jena](ops, JenaOperations)
    val jenaGraph = mToJena.transform(graph)
    println(JenaRDFWriter.turtleWriter.asString(jenaGraph, ""))
  }

  def copy(graph: Jena#Graph): Jena#Graph = {
    graph match {
      case bjg @ BareJenaGraph(_) => {
        val g = Factory.createDefaultGraph
        val it = graph.jenaGraph.find(JenaNode.ANY, JenaNode.ANY, JenaNode.ANY)
        while (it.hasNext) { g.add(it.next()) }
        BareJenaGraph(g)
      }
      case other => other
    }
  }

}
