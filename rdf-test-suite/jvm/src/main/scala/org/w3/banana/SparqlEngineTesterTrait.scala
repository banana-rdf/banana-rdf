package org.w3.banana

import java.io.FileInputStream

import org.scalatest.{BeforeAndAfterAll, Suite}
import org.w3.banana.io._

import scala.util.Try
import scalaz.{Comonad, Monad}
import scalaz.syntax._
import comonad._

// TODO
trait SparqlEngineTesterTrait[Rdf <: RDF, M[+_], A] extends BeforeAndAfterAll { self: Suite =>
  val store: A
  val reader: RDFReader[Rdf, Try, RDFXML]
  implicit val ops: RDFOps[Rdf]
  implicit val graphStore: GraphStore[Rdf, M, A]
  implicit val monad:  Monad[M]
  implicit val comonad: Comonad[M]

  val lifecycle: Lifecycle[Rdf, A]

  // both Monad and Comonad are Functors, so they compete for the
  // syntax. So we choose arbitrarily one of them.
  // TODO @betehess to ask scalaz people
  val M = Monad[M]
  import M.monadSyntax._
  import graphStore.graphStoreSyntax._
  import lifecycle.lifecycleSyntax._
  import ops._
  import org.w3.banana.diesel._

  override protected def afterAll(): Unit = {
    store.stop()
  }

  val foaf = FOAFPrefix(ops)

  val resource = new FileInputStream("rdf-test-suite/jvm/src/main/resources/new-tr.rdf")

  val graph = reader.read(resource, "http://example.com") getOrElse sys.error("ouch")

  val graph1: Rdf#Graph = (
    bnode("betehess")
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.title ->- "Mr").graph

  val graph2: Rdf#Graph = (
    bnode("betehess")
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.knows ->- (
      URI("http://bblfish.net/#hjs")
      -- foaf.name ->- "Henry Story"
      -- foaf.currentProject ->- URI("http://webid.info/"))).graph

  abstract override protected def beforeAll(): Unit = {
    store.start()
    val init =
      for {
        _ <- store.appendToGraph(URI("http://example.com/graph1"), graph1)
        _ <- store.appendToGraph(URI("http://example.com/graph2"), graph2)
        _ <- store.appendToGraph(URI("http://example.com/graph"), graph)
      } yield ()
    init.copoint
  }

}
