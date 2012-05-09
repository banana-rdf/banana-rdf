package org.w3.banana.jena

import org.w3.banana._
import scala.collection.JavaConverters._

object JenaGraphTraversal extends RDFGraphTraversal[Jena] {

  import JenaOperations._
  import com.hp.hpl.jena.rdf.model.{ Literal => JenaLiteral, _ }
  import com.hp.hpl.jena.rdf.model.ResourceFactory._

  val toNodeVisitor: RDFVisitor = new RDFVisitor {
    def visitBlank(r: Resource, id: AnonId) = BNode(id.getLabelString)
    // go from the model's Node to the Graph one, then cast it (always safe here)
    def visitLiteral(l: JenaLiteral) = Literal.fold(l.asNode.asInstanceOf[Jena#Literal])(x => x, x => x)
    def visitURI(r: Resource, uri: String) = IRI(uri)
  }

  def toNode(rdfNode: RDFNode): Jena#Node = rdfNode.visitWith(toNodeVisitor).asInstanceOf[Jena#Node]

  def getObjects(graph: Jena#Graph, subject: Jena#Node, predicate: Jena#IRI): Iterable[Jena#Node] = {
    val model = ModelFactory.createModelForGraph(graph)
    val subjectResource = Node.fold(subject)(
      { case IRI(s) => model.createResource(s) },
      { case BNode(label) => model.createResource(new AnonId(label)) },
      lit => throw new RuntimeException("shouldn't use a literal here")
    )
    val IRI(p) = predicate
    val it: Iterator[Jena#Node] = model.listObjectsOfProperty(subjectResource, createProperty(p)).asScala map toNode
    new Iterable[Jena#Node] {
      def iterator = it
    }
  }

}
