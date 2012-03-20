package org.w3.linkeddata

import org.w3.rdf._

import scala.collection.JavaConverters._
import scala.collection.mutable.ConcurrentMap
import akka.dispatch._
import akka.util.duration._
import com.ning.http.client._
import java.util.concurrent.{ConcurrentHashMap, ExecutorService, Executors, Future ⇒ jFuture}

class LinkedDataMemoryKB[Rdf <: RDF](
    val ops: RDFOperations[Rdf],
    val projections: Projections[Rdf],
    val utils: RDFUtils[Rdf],
    val turtleReader: RDFReader[Rdf, Turtle],
    val turtleWriter: TurtleWriter[Rdf]) extends LinkedData[Rdf] {

  import ops._
  import projections._
  import utils._

  val logger = new Object {
    def debug(msg: => String): Unit = println(msg)
  }

  val executor: ExecutorService = Executors.newFixedThreadPool(10)

  implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutorService(executor)

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

  val kb: ConcurrentMap[IRI, Future[Graph]] = new ConcurrentHashMap[IRI, Future[Graph]]().asScala

  def shutdown(): Unit = {
    logger.debug("shuting down the Linked Data facade")
    httpClient.close()
    executor.shutdown()
  }

  def goto(iri: IRI): LD[IRI] = {
    val supportDoc = iri.supportDocument
    if (!kb.isDefinedAt(supportDoc)) {
      val IRI(iri) = supportDoc
      val futureGraph: Future[Graph] = Future {
        logger.debug("GET " + iri)
        val is = httpClient.prepareGet(iri).setHeader("Accept", "text/rdf+n3").execute().get().getResponseBodyAsStream()
        val graph = turtleReader.read(is, iri).fold(f ⇒ throw f, g ⇒ g)
        graph
      }
      // andThen {
      //   case t => println(t)
      // }
      kb.put(supportDoc, futureGraph)
    }
    new LD(Promise.successful(iri))
  }

  def point[S](s: S): LD[S] = new LD[S](Promise.successful(s))

  class LD[S](val innerFuture: Future[S]) extends super.LDInterface[S] {

    import ops._

    /*
     * yes, 60 seconds is a bit long but dbpedia is freaking slooooow
     */
    def timbl(): S = Await.result(innerFuture, 60 seconds)

    def map[A](f: S ⇒ A): LD[A] = new LD[A](innerFuture map f)

    def flatMap[A](f: S ⇒ LD[A]): LD[A] = new LD[A](
      for {
        value ← innerFuture
        result ← f(value).innerFuture
      } yield result)

    def follow(predicate: IRI)(implicit ev: S =:= IRI): LD[Iterable[Node]] = new LD(
      for {
        subject ← innerFuture map ev
        supportDocument = subject.supportDocument
        graph ← kb.get(supportDocument) getOrElse sys.error("something is really wrong")
      } yield {
        val os = graph.getObjects(subject, predicate)
        os
      })

    def followAll(predicate: IRI)(implicit ev: S =:= Iterable[Node]): LD[Iterable[Node]] = {
      val f = innerFuture.map(ev) flatMap { (nodes: Iterable[Node]) ⇒
        val nodesFuture: Iterable[Future[Iterable[Node]]] =
          nodes.map { (node: Node) =>
            node.fold[Future[Iterable[Node]]](
              iri => goto(iri).follow(predicate).innerFuture,
              bn => Promise.successful(Iterable.empty),
              lit => Promise.successful(Iterable.empty)
            )}
        val futureNodes: Future[Iterable[Iterable[Node]]] = Future.sequence(nodesFuture)
        futureNodes map (_.flatten)
      }
      new LD(f)
    }

    def asURIs()(implicit ev: S =:= Iterable[Rdf#Node]): LD[Iterable[IRI]] = new LD(
      innerFuture map { nodes ⇒
        nodes.flatMap {
          _.fold[Option[Rdf#IRI]](
            iri ⇒ Some(iri),
            bnode ⇒ None,
            literal ⇒ None)
        }
      })

  }
}
