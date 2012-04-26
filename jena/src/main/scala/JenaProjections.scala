package org.w3.rdf.jena

import org.w3.rdf._
import scala.collection.JavaConverters._

object JenaProjections extends Projections[Jena] {

  import JenaOperations._
  import com.hp.hpl.jena.rdf.model.{ Literal => JenaLiteral, _ }
  import com.hp.hpl.jena.rdf.model.ResourceFactory._

  val visitor: RDFVisitor = new RDFVisitor {
    def visitBlank(r: Resource, id: AnonId) = BNode(id.getLabelString)
    def visitLiteral(l: JenaLiteral) = Literal.fold(l.asNode.asInstanceOf[Literal])(x => x, x => x) //TypedLiteral(l.getLexicalForm)
    def visitURI(r: Resource, uri: String) = IRI(uri)
  }

  def toNode(rdfNode: RDFNode): Node = rdfNode.visitWith(visitor).asInstanceOf[Node]

  def getObjects(graph: Jena#Graph, subject: Jena#Node, predicate: Jena#IRI): Iterable[Jena#Node] = {
    val model = ModelFactory.createModelForGraph(graph)
    val subjectResource = Node.fold(subject)(
      { case IRI(s) => model.createResource(s) },
      { case BNode(label) => model.createResource(new AnonId(label)) },
      lit => throw new RuntimeException("shouldn't use a literal here")
    )
    val IRI(p) = predicate
    val it: Iterator[Node] = model.listObjectsOfProperty(subjectResource, createProperty(p)).asScala map toNode
    new Iterable[Node] {
      def iterator = it
    }
  }
}
