package org.w3.banana

import java.io.FileInputStream

import org.scalatest.{ Suite, BeforeAndAfterAll }
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by hjs on 01/09/2014.
 */
trait SparqlEngineTesterTrait[Rdf <: RDF, A]
    extends BeforeAndAfterAll { self: Suite =>
  val store: A
  val reader: RDFReader[Rdf, RDFXML]
  implicit val ops: RDFOps[Rdf]
  implicit val graphStore: GraphStore[Rdf, A]
  val lifecycle: Lifecycle[Rdf, A]

  import ops._
  import graphStore.graphStoreSyntax._
  import lifecycle.lifecycleSyntax._
  import diesel._

  abstract override protected def afterAll(): Unit = {
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
    init.getOrFail()
  }

}
