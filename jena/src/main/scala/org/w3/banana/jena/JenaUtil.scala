package org.w3.banana.jena

import org.apache.jena.graph.{Factory, Node as JenaNode}
import org.apache.jena.rdf.model.{Literal as JenaLiteral, *}
import org.w3.banana.*

class JenaUtil(using ops: Ops[JenaRdf.type]):

   type R = JenaRdf.type

   import ops.{given, *}

   val toNodeVisitor: RDFVisitor = new RDFVisitor:
      def visitBlank(r: Resource, id: AnonId) = BNode(id.getLabelString.nn)

      def visitLiteral(l: JenaLiteral) = l.asNode.asInstanceOf[RDF.Literal[R]]

      def visitURI(r: Resource, uri: String) = URI(uri)

   def toNode(rdfNode: RDFNode): RDF.Node[R] =
     rdfNode.visitWith(toNodeVisitor).asInstanceOf[RDF.Node[R]]
