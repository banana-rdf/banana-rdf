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




object LinkedData {

  import java.util.concurrent.{ ExecutorService, Executors }

  val executor: ExecutorService = Executors.newFixedThreadPool(10)

  implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutorService(executor)

}



class LinkedData[Rdf <: RDF](
  val ops: RDFOperations[Rdf],
  val projections: Projections[Rdf],
  val utils: RDFUtils[Rdf],
  val turtleReader: RDFReader[Rdf, Turtle],
  val turtleWriter: TurtleWriter[Rdf]) {

  import ops._
  import projections._
  import utils._

  import LinkedData._

  import com.ning.http.client._
  import java.util.concurrent.{ Future => jFuture }

  val httpClient = new AsyncHttpClient

  private val kb: ConcurrentMap[IRI, Future[Graph]] = new ConcurrentHashMap[IRI, Future[Graph]]().asScala

  def follow(subject: IRI, predicate: IRI): Future[Iterable[Rdf#Node]] = {
    // ex: http://www.w3.org/People/Berners-Lee/card#i  ~~>  http://www.w3.org/People/Berners-Lee/card
    val supportDocument = subject.supportDocument

    val futureGraph: Future[Graph] = kb.get(supportDocument) getOrElse {
      val IRI(iri) = supportDocument
      val f: Future[Graph] = Future {
        val is = httpClient.prepareGet(iri).setHeader("Accept", "text/rdf+n3").execute().get().getResponseBodyAsStream()
        val graph = turtleReader.read(is, iri).fold(f => throw f, g => g)
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
