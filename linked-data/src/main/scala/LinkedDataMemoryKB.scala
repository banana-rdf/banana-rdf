package org.w3.linkeddata

import org.w3.banana._

import scala.collection.JavaConverters._
import scala.collection.mutable.ConcurrentMap
import akka.actor._
import akka.dispatch._
import akka.util._
import akka.util.duration._
import com.ning.http.client._
import java.util.concurrent.{ConcurrentHashMap, ExecutorService, Executors, Future ⇒ jFuture}
import org.w3.util._
import org.w3.util.FutureValidation._
import org.w3.util.Pimps._
import scalaz._

class LinkedDataMemoryKB[Rdf <: RDF](
    val ops: RDFOperations[Rdf],
    val graphTraversal: RDFGraphTraversal[Rdf],
    val utils: RDFUtils[Rdf],
    val readerFactory: RDFReaderFactory[Rdf]) extends LinkedData[Rdf] {

  import ops._
  import graphTraversal._
  import utils._
  import readerFactory._

  val xsd = XSDPrefix(ops)

  val logger = new Object {
    def debug(msg: => String): Unit = println(msg)
  }

  val system = ActorSystem("foo")
  implicit val dispatcher = system.dispatcher

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

  val kb: ConcurrentMap[Rdf#URI, FutureValidation[LDError, Rdf#Graph]] = new ConcurrentHashMap[Rdf#URI, FutureValidation[LDError, Rdf#Graph]]().asScala

  def shutdown(): Unit = {
    logger.debug("shuting down the Linked Data facade")
    httpClient.close()
    executor.shutdown()
    system.shutdown()
  }

  def goto(iri: Rdf#URI): LD[Rdf#URI] = {
    val supportDoc = supportDocument(iri)
    if (!kb.isDefinedAt(supportDoc)) {
      val URI(iri) = supportDoc
      val futureGraph: FutureValidation[LDError, Rdf#Graph] = delayedValidation {
        logger.debug("GET " + iri)
        val response = httpClient.prepareGet(iri).setHeader("Accept", "application/rdf+xml, text/rdf+n3, text/turtle").execute().get()
        val is = response.getResponseBodyAsStream()
        response.getHeader("Content-Type").split(";")(0) match {
          case "text/n3" | "text/turtle" => TurtleReader.read(is, iri) failMap { t => ParsingError(t.getMessage) }
          case "application/rdf+xml" => RDFXMLReader.read(is, iri) failMap { t => ParsingError(t.getMessage) }
          case ct => Failure[LDError, Rdf#Graph](UnknownContentType(ct))
        }
      }
      kb.put(supportDoc, futureGraph)
    }
    point(iri)
  }

  def point[S](s: S): LD[S] = new LD[S](immediateValidation(Success(s)))

  def pointFailure[S](f: LDError): LD[S] = new LD[S](immediateValidation(Failure(f)))

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

    def foreach(f: S => Unit): Unit = underlying foreach f

    def followURI(predicate: Rdf#URI)(implicit ev: S =:= Rdf#URI): LD[Iterable[Rdf#Node]] = new LD(
      for {
        subject ← underlying map ev
        supportDoc = supportDocument(subject)
        graph ← kb.get(supportDoc) getOrElse sys.error("something is really wrong")
      } yield {
        val objects = getObjects(graph, subject, predicate)
        objects
      }
    )

    def follow(
      predicate: Rdf#URI,
      max: Int = 10,
      maxDownloads: Int = 10)(
      implicit ev: S =:= Iterable[Rdf#Node]): LD[Iterable[Rdf#Node]] = {
      val f = underlying.map(ev) flatMap { (nodes: Iterable[Rdf#Node]) ⇒
        val nodesFutureValidation: Iterable[FutureValidation[LDError, Iterable[Rdf#Node]]] =
          nodes.take(maxDownloads).map { (node: Rdf#Node) =>
            node.fold[FutureValidation[LDError, Iterable[Rdf#Node]]](
              iri => goto(iri).followURI(predicate).underlying,
              bn => immediateValidation(Success(Iterable.empty)),
              lit => immediateValidation(Success(Iterable.empty))
            )}
        val nodesFuture: Iterable[Future[Validation[LDError, Iterable[Rdf#Node]]]] =
          nodesFutureValidation map { _.asFuture }
        implicit val timeout: Timeout = 10.seconds
        val promiseStream = PromiseStream[Validation[LDError, Iterable[Rdf#Node]]]()
        for {
          future <- nodesFuture
        } promiseStream += future
        val stream = Iterator.continually(promiseStream.dequeue())
        val futureNodes: Future[Iterable[Validation[LDError, Iterable[Rdf#Node]]]] =
          Future.sequence {
            val foo = stream.take(max).toSeq
            println(";;; "+foo.size)
            foo
          }
        // as some point, we'll want to accumulate the errors somewhere,
        // still not failing because of the open world model
        val futureSuccesses: Future[Iterable[Iterable[Rdf#Node]]] =
          futureNodes map { _ collect { case Success(nodes) => nodes } }
        val futureResult: Future[Validation[LDError, Iterable[Rdf#Node]]] =
          futureSuccesses map { ititnodes => Success(ititnodes.flatten) }
        FutureValidation(futureResult)
      }
      new LD(f)
    }

    def as[T](f: Rdf#Node => Option[T])(implicit ev: S =:= Iterable[Rdf#Node]): LD[Iterable[T]] =
      new LD(
        underlying map { nodes => ev(nodes).map(f).flatten }
      )

    def asURIs(implicit ev: S =:= Iterable[Rdf#Node]): LD[Iterable[Rdf#URI]] = {
      def f(node: Rdf#Node): Option[Rdf#URI] = 
        node.fold[Option[Rdf#URI]](
          iri ⇒ Some(iri),
          bnode ⇒ None,
          literal ⇒ None)
      as[Rdf#URI](f)
    }

    def asStrings(implicit ev: S =:= Iterable[Rdf#Node]): LD[Iterable[String]] = {
      def f(node: Rdf#Node): Option[String] =
        node.fold[Option[String]](
          iri ⇒ None,
          bnode ⇒ None,
          _.fold(
            {
              case TypedLiteral(lexicalForm, datatype) =>
                if (datatype == xsd.string)
                  Some(lexicalForm)
                else
                  None
            },
            langlit => None
          )
        )
      as[String](f)
    }

    def asInts(implicit ev: S =:= Iterable[Rdf#Node]): LD[Iterable[Int]] = {
      def f(node: Rdf#Node): Option[Int] = 
        node.fold[Option[Int]](
          iri ⇒ None,
          bnode ⇒ None,
          _.fold(
            {
              case TypedLiteral(lexicalForm, datatype) =>
                if (datatype == xsd.int)
                  try Some(lexicalForm.toInt) catch { case nfe: NumberFormatException => None }
                else
                  None
            },
            langlit => None
          )
        )
      as[Int](f)
    }

  }
}
