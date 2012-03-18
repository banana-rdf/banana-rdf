package org.w3.linkeddata

import org.w3.rdf._

import org.w3.rdf.sesame._
import org.w3.rdf.jena._

import scala.collection.JavaConverters._
import scala.collection.mutable.ConcurrentMap
import java.util.concurrent.ConcurrentHashMap

import akka.dispatch._
import akka.util.duration._

import scalaz._
import Scalaz._

trait Projections[Rdf <: RDF] { self =>
  class GraphW(graph: Rdf#Graph) {
    def getObjects(subject: Rdf#IRI, predicate: Rdf#IRI): Iterable[Rdf#Node] =
      self.getObjects(graph, subject, predicate)
  }
  implicit def decorateGraphWithProjections(graph: Rdf#Graph) = new GraphW(graph)
  def getObjects(graph: Rdf#Graph, subject: Rdf#IRI, predicate: Rdf#IRI): Iterable[Rdf#Node]
}

object SesameProjections extends Projections[Sesame] {
  import SesameOperations._
  def getObjects(graph: Graph, subject: IRI, predicate: IRI): Iterable[Node] = {
    import org.openrdf.model.util._
    new Iterable[Node] {
      def iterator = GraphUtil.getObjectIterator(graph, subject, predicate).asScala
    }
  }
}

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

trait RDFUtils[Rdf <: RDF] { self =>
  class IRIW(iri: Rdf#IRI) {
    def supportDocument: Rdf#IRI =
      self.supportDocument(iri)
  }
  implicit def wrapIRI(iri: Rdf#IRI) = new IRIW(iri)
  def supportDocument(iri: Rdf#IRI): Rdf#IRI
}

class RDFUtilsImpl[Rdf <: RDF](val ops: RDFOperations[Rdf]) extends RDFUtils[Rdf] {

  import ops._

  def supportDocument(iri: IRI): IRI = {
    val IRI(iriString) = iri
    val uri = new java.net.URI(iriString)
    import uri._
    val uriNoFrag = new java.net.URI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, null)
    IRI(uriNoFrag.toString)
  }

}

object SesameRDFUtils extends RDFUtilsImpl(SesameOperations)
object JenaRDFUtils extends RDFUtilsImpl(JenaOperations)
