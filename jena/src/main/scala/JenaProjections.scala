package org.w3.rdf.jena

import org.w3.rdf._
import scala.collection.JavaConverters._


object JenaProjections extends Projections[Jena] {

  import JenaOperations._
  import com.hp.hpl.jena.rdf.model.{ Literal => JenaLiteral, _ }
  import com.hp.hpl.jena.rdf.model.ResourceFactory._

  val visitor: RDFVisitor = new RDFVisitor {
    def visitBlank(r: Resource, id: AnonId) = BNode(id.getLabelString)
    def visitLiteral(l: JenaLiteral) = TypedLiteral(l.getLexicalForm)
    def visitURI(r: Resource, uri: String) = IRI(uri)
  }
  def toNode(rdfNode: RDFNode): Node = rdfNode.visitWith(visitor).asInstanceOf[Node]

  def getObjects(graph: Graph, subject: IRI, predicate: IRI): Iterable[Node] = {
    val model = ModelFactory.createModelForGraph(graph)
    val IRI(s) = subject
    val IRI(p) = predicate
    val it: Iterator[Node] = model.listObjectsOfProperty(createResource(s), createProperty(p)).asScala map toNode
    new Iterable[Node] {
      def iterator = it
    }
  }
}
