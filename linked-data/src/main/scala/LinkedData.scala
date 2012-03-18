package org.w3.linkeddata

import org.w3.rdf._

import org.w3.rdf.sesame._
import org.w3.rdf.jena._

import scala.collection.JavaConverters._
import scala.collection.mutable.ConcurrentMap
import java.util.concurrent.ConcurrentHashMap

import akka.dispatch._
import akka.util.duration._

// import scalaz._
// import Scalaz._
// import Validation._


object LinkedData {

  import java.util.concurrent.{ ExecutorService, Executors }

  val executor: ExecutorService = Executors.newFixedThreadPool(10)

  implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutorService(executor)

  import com.ning.http.client._
  import java.util.concurrent.{ Future => jFuture }
  val httpClient = {
    val timeout: Int = 2000
    val executor = Executors.newCachedThreadPool()
    val builder = new AsyncHttpClientConfig.Builder()
    val config =
      builder.setMaximumConnectionsTotal(1000)
      .setMaximumConnectionsPerHost(15)
      .setExecutorService(executor)
      .setFollowRedirects(true)
      .setConnectionTimeoutInMs(timeout)
      .build
    new AsyncHttpClient(config)
  }


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


  private val kb: ConcurrentMap[IRI, Future[Graph]] = new ConcurrentHashMap[IRI, Future[Graph]]().asScala

  class IRIW(iri: IRI) {
    def follow(predicate: IRI): LD[Iterable[Node]] = goto(iri).follow(predicate)
  }

  implicit def wrapIRI(iri: IRI): IRIW = new IRIW(iri)

  class IRIsW(iris: Iterable[IRI]) {
    def follow(predicate: IRI): LD[Iterable[Node]] = {
      val irisLD = new LD[Iterable[IRI]](Promise.successful(iris))
      irisLD.followAll(predicate)
    }
  }

  implicit def wrapIRIs(iris: Iterable[IRI]): IRIsW = new IRIsW(iris)

  def goto(iri: IRI): LD[IRI] = {
    val supportDoc = iri.supportDocument
    if (! kb.isDefinedAt(supportDoc)) {
      val IRI(iri) = supportDoc
      val futureGraph: Future[Graph] = Future {
        val is = httpClient.prepareGet(iri).setHeader("Accept", "text/rdf+n3").execute().get().getResponseBodyAsStream()
        val graph = turtleReader.read(is, iri).fold(f => throw f, g => g)
        graph
      }
      // andThen {
      //   case t => println(t)
      // }
      kb.put(supportDoc, futureGraph)
    }
    new LD(Promise.successful(iri))
  }


  class LD[S](val future: Future[S]) {

    def timbl(): S = Await.result(future, 5 seconds)

    def map[A](f: S => A): LD[A] = new LD[A](future map f)

    def flatMap[A](f: S => LD[A]): LD[A] = new LD[A] (
      for {
        value <- future
        result <- f(value).future
      } yield result
    )

    // def getGraph()(implicit ev: S =:= IRI): LD[Graph] = new LD(
    //   for {
    //     subject <- future map ev
    //     supportDocument = subject.supportDocument
    //     graph <- kb.get(supportDocument) getOrElse sys.error("something is really wrong")
    //   } yield     graph
    //   }
    // )
      

    def follow(predicate: IRI)(implicit ev: S =:= IRI): LD[Iterable[Node]] = new LD (
      for {
        subject <- future map ev
        supportDocument = subject.supportDocument
        graph <- kb.get(supportDocument) getOrElse sys.error("something is really wrong")
      } yield {
        val os = graph.getObjects(subject, predicate)
        os
      }
    )

    def followAll(predicate: IRI)(implicit ev: S =:= Iterable[IRI]): LD[Iterable[Node]] = {
      val f = future map ev flatMap { iris =>
        val nodesFuture: Iterable[Future[Iterable[Node]]] = iris map (iri => goto(iri).follow(predicate).future)
        val nodes: Future[Iterable[Iterable[Node]]] = Future.sequence(nodesFuture)
        nodes map (_.flatten)
      }
      new LD(f)
    }


    def asURIs()(implicit ev: S =:= Iterable[Rdf#Node]): LD[Iterable[IRI]] = new LD (
      future map { nodes =>
        nodes.flatMap { _.fold[Option[Rdf#IRI]](
          iri => Some(iri),
          bnode => None,
          literal => None)
        }
      }
    )

  }
}

object Main {

  def main(args: Array[String]): Unit = {

    //    import SesameOperations._
    //    
    //    val ld = new LinkedData(SesameOperations, SesameProjections, SesameRDFUtils, SesameTurtleReader, SesameTurtleWriter)

    import JenaOperations._

    val ld = new LinkedData(JenaOperations, JenaProjections, JenaRDFUtils, JenaTurtleReader, JenaTurtleWriter)
    import ld._

//    val mbox = goto(IRI("http://www.w3.org/People/Berners-Lee/card#i")).follow(IRI("http://xmlns.com/foaf/0.1/mbox")).asURIs().timbl()


    // val barack = goto(IRI("http://dbpedia.org/resource/Barack_Obama"))
    // val children = barack.follow(IRI("http://dbpedia.org/ontology/child")).follow("http://dbpedia.org/property/members").follow("")



    val namesLD = for {
      barack <- goto(IRI("http://dbpedia.org/resource/Barack_Obama"))
      family <- barack.follow(IRI("http://dbpedia.org/ontology/child")).asURIs()
      members <- family.follow(IRI("http://dbpedia.org/property/members")).asURIs()
      names <- members.follow(IRI("http://dbpedia.org/property/name"))
    } yield names

    val names = namesLD.timbl()

    println(names)

    // val mboxLD: LD[IRI] = for {
    //   barac <- goto(IRI("http://www.w3.org/People/Berners-Lee/card#i"))
    //   mbox <- tim.follow(IRI("http://xmlns.com/foaf/0.1/mbox"))
      
      
    //              //.asURIs()
    // } yield subject //objects

    // val mbox = mboxLD.timbl()

//    println(mbox)

    LinkedData.executor.shutdown()
    LinkedData.httpClient.close()

  }

}
