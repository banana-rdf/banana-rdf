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
  class GraphW(graph:Rdf#Graph) {
    def getObjects(subject: Rdf#IRI, predicate:Rdf#IRI): Iterable[Rdf#Node] = 
      self.getObjects(graph, subject, predicate)
  }
  implicit def decorateGraphWithProjections(graph: Rdf#Graph) = new GraphW(graph)
  def getObjects(graph: Rdf#Graph, subject: Rdf#IRI, predicate:Rdf#IRI): Iterable[Rdf#Node]
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
  import com.hp.hpl.jena.rdf.model.{Literal => JenaLiteral, _}
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
    IRI(uriNoFrag.toString+".ttl")
  }
  
}

object SesameRDFUtils extends RDFUtilsImpl(SesameOperations)
object JenaRDFUtils extends RDFUtilsImpl(JenaOperations)

object LinkedData {
  
  import java.util.concurrent.{ExecutorService, Executors}
  
  val executor: ExecutorService = Executors.newFixedThreadPool(10)
  
  implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutorService(executor)

}

class LinkedData[Rdf <: RDF](
    val ops: RDFOperations[Rdf],
    val projections: Projections[Rdf],
    val utils: RDFUtils[Rdf],
    val turtleReader: TurtleReader[Rdf],
    val turtleWriter: TurtleWriter[Rdf]) {
  
  import ops._
  import projections._
  import utils._
  
  import LinkedData._
  
  private val kb: ConcurrentMap[IRI, Future[Graph]] = new ConcurrentHashMap[IRI, Future[Graph]]().asScala
  
  def follow(subject: IRI, predicate: IRI): Future[Iterable[Rdf#Node]] = {
    // ex: http://www.w3.org/People/Berners-Lee/card#i  ~~>  http://www.w3.org/People/Berners-Lee/card
    val supportDocument = subject.supportDocument
    
    val futureGraph:Future[Graph] = kb.get(supportDocument) getOrElse {
      val IRI(iri) = supportDocument
      val f: Future[Graph] = Future {
        val reader = scala.io.Source.fromURL(iri).bufferedReader
        val graph = turtleReader.read(reader, iri).fold( f => throw f, g => g)
        graph
      }
      kb.put(supportDocument, f)
      f
    }
      
    futureGraph map { graph =>
      graph.getObjects(subject, predicate) // : Iterable[Node]
    }
    
  }
  

  
  
}




object Main {
  
  def main(args: Array[String]): Unit = {
    
//    import SesameOperations._
//    
//    val ld = new LinkedData(SesameOperations, SesameProjections, SesameRDFUtils, SesameTurtleReader, SesameTurtleWriter)

    import JenaOperations._
    
    val ld = new LinkedData(JenaOperations, JenaProjections, JenaRDFUtils, JenaTurtleReader, JenaTurtleWriter)

    val f = ld.follow(IRI("http://www.w3.org/People/Berners-Lee/card#i"), IRI("http://xmlns.com/foaf/0.1/mbox"))
    val objects = Await.result(f, 5 seconds)
    
    println(objects)
    
    LinkedData.executor.shutdown()
    
  }
  
  
}