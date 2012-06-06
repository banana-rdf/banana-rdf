package org.w3.banana.jena

import org.w3.banana._
import scala.collection.JavaConverters._
import JenaOperations._
import com.hp.hpl.jena.rdf.model.{ Literal => JenaLiteral, _ }
import com.hp.hpl.jena.rdf.model.ResourceFactory._
import com.hp.hpl.jena.graph.{ Node => JenaNode, _ }
import com.hp.hpl.jena.util.iterator._

object JenaGraphTraversal extends RDFGraphTraversal[Jena] {

  val toNodeVisitor: RDFVisitor = new RDFVisitor {
    def visitBlank(r: Resource, id: AnonId) = BNode(id.getLabelString)
    // go from the model's Node to the Graph one, then cast it (always safe here)
    def visitLiteral(l: JenaLiteral) = Literal.fold(l.asNode.asInstanceOf[Jena#Literal])(x => x, x => x)
    def visitURI(r: Resource, uri: String) = URI(uri)
  }

  def toNode(rdfNode: RDFNode): Jena#Node = rdfNode.visitWith(toNodeVisitor).asInstanceOf[Jena#Node]

  def getObjects(graph: Jena#Graph, subject: Jena#Node, predicate: Jena#URI): Iterable[Jena#Node] = {
    val model = ModelFactory.createModelForGraph(graph)
    val subjectResource = Node.fold(subject)(
      { case URI(s) => model.createResource(s) },
      { case BNode(label) => model.createResource(new AnonId(label)) },
      lit => throw new RuntimeException("shouldn't use a literal here")
    )
    val URI(p) = predicate
    val it: Iterator[Jena#Node] = model.listObjectsOfProperty(subjectResource, createProperty(p)).asScala map toNode
    new Iterable[Jena#Node] {
      def iterator = it
    }
  }

  def getPredicates(graph: Jena#Graph, subject: Jena#Node): Iterable[Jena#URI] = {
    val predicates: Iterator[Jena#URI] = graph.find(subject, JenaNode.ANY, JenaNode.ANY).asScala map { triple => triple.getPredicate().asInstanceOf[Node_URI]}
    new Iterable[Jena#URI] {
      def iterator = predicates
    }
  }

  def getSubjects(graph: Jena#Graph, predicate: Jena#URI, obj: Jena#Node): Iterable[Jena#Node] = {
    val subjects: Iterator[Jena#Node] = graph.find(JenaNode.ANY, JenaNode.ANY, obj).asScala map { triple => triple.getSubject()}
    new Iterable[Jena#Node] {
      def iterator = subjects
    }
  }

}
