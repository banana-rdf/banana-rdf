package org.w3.linkeddata

import org.w3.rdf._

import scala.collection.JavaConverters._
import scala.collection.mutable.ConcurrentMap
import akka.dispatch._
import akka.util.Duration
import akka.util.duration._
import com.ning.http.client._
import java.util.concurrent.{ConcurrentHashMap, ExecutorService, Executors, Future ⇒ jFuture}
import org.w3.util._
import org.w3.util.FutureValidation._
import org.w3.util.Pimps._
import scalaz._

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

  val kb: ConcurrentMap[IRI, FutureValidation[LDError, Graph]] = new ConcurrentHashMap[IRI, FutureValidation[LDError, Graph]]().asScala

  def shutdown(): Unit = {
    logger.debug("shuting down the Linked Data facade")
    httpClient.close()
    executor.shutdown()
  }

  def goto(iri: IRI): LD[IRI] = {
    val supportDoc = iri.supportDocument
    if (!kb.isDefinedAt(supportDoc)) {
      val IRI(iri) = supportDoc
      val futureGraph: FutureValidation[LDError, Graph] = delayedValidation {
        logger.debug("GET " + iri)
        val is = httpClient.prepareGet(iri).setHeader("Accept", "text/rdf+n3").execute().get().getResponseBodyAsStream()
        val graph = turtleReader.read(is, iri) failMap { t => ParsingError }
        graph
      }
      kb.put(supportDoc, futureGraph)
    }
    point(iri)
  }

  def point[S](s: S): LD[S] = new LD[S](immediateValidation(Success(s)))

  class LD[S](val underlying: FutureValidation[LDError, S]) extends super.LDInterface[S] {

    import ops._

    /*
     * yes, 60 seconds is a bit long but dbpedia is freaking slooooow
     */
    def timbl(atMost: Duration = 60.seconds): Validation[LDError, S] =
      Await.result(underlying.asFuture, atMost)

    def map[A](f: S ⇒ A): LD[A] = new LD[A](underlying map f)

    def flatMap[A](f: S ⇒ LD[A]): LD[A] = new LD[A](
      for {
        value ← underlying
        result ← f(value).underlying
      } yield result
    )

    def follow(predicate: IRI)(implicit ev: S =:= IRI): LD[Iterable[Node]] = new LD(
      for {
        subject ← underlying map ev
        supportDocument = subject.supportDocument
        graph ← kb.get(supportDocument) getOrElse sys.error("something is really wrong")
      } yield {
        val objects = graph.getObjects(subject, predicate)
        objects
      }
    )

    def followAll(predicate: IRI)(implicit ev: S =:= Iterable[Node]): LD[Iterable[Node]] = {
      val f = underlying.map(ev) flatMap { (nodes: Iterable[Node]) ⇒
        val nodesFutureValidation: Iterable[FutureValidation[LDError, Iterable[Node]]] =
          nodes.map { (node: Node) =>
            node.fold[FutureValidation[LDError, Iterable[Node]]](
              iri => goto(iri).follow(predicate).underlying,
              bn => immediateValidation(Success(Iterable.empty)),
              lit => immediateValidation(Success(Iterable.empty))
            )}
        val nodesFuture: Iterable[Future[Validation[LDError, Iterable[Node]]]] =
          nodesFutureValidation map { _.asFuture }
        val futureNodes: Future[Iterable[Validation[LDError, Iterable[Node]]]] =
          Future.sequence(nodesFuture)
        // as some point, we'll want to accumulate the errors somewhere,
        // still not failing because of the open world model
        val futureSuccesses: Future[Iterable[Iterable[Node]]] =
          futureNodes map { _ collect { case Success(nodes) => nodes } }
        val futureResult: Future[Validation[LDError, Iterable[Node]]] =
          futureSuccesses map { ititnodes => Success(ititnodes.flatten) }
        FutureValidation(futureResult)
      }
      new LD(f)
    }

    def asURIs(implicit ev: S =:= Iterable[Rdf#Node]): LD[Iterable[IRI]] = new LD(
      underlying map { nodes ⇒
        nodes.flatMap {
          _.fold[Option[Rdf#IRI]](
            iri ⇒ Some(iri),
            bnode ⇒ None,
            literal ⇒ None)
        }
      })

  }
}
