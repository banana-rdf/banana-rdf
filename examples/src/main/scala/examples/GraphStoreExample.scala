package org.w3.banana.examples

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import scala.concurrent.ExecutionContext.Implicits.global
import org.w3.banana._

/* Here is an example storing in a local RDF store:
 * - triples locally added or removed
 * - triples read from Internet
 * 
 * See general explanations in IOExample.scala.
 * 
 * To run this example from sbt:
 *   project examples
 *   run-main org.w3.banana.examples.GraphExampleWithJena
 *   run-main org.w3.banana.examples.GraphExampleWithSesame
 */
abstract class GraphStoreExample[Rdf <: RDF, Store](
    implicit ops: RDFOps[Rdf],
    turtleReader: RDFReader[Rdf, Turtle],
    rdfXMLWriter: RDFWriter[Rdf, RDFXML],
    rdfStore: RDFStore[Rdf, Store]) {

  import ops._
  import rdfStore.graphStoreSyntax._

  def makeRDFStore(file: String): Store

  def main(args: Array[String]): Unit = {

    /* reads TimBL's card in Turtle */

    val timblCard = "http://www.w3.org/People/Berners-Lee/card.ttl"
    val graph = TurtleReader.read(from, base = timblCard) getOrElse sys.error("couldn't read TimBL's card")

    val jmvCard = "http://jmvanel.free.fr/jmv.rdf"
    val foaf = FOAFPrefix(Ops)
    val xsd = XSDPrefix(Ops)
    val timsURI = makeUri(timblCard + "#i")
    val from = new java.net.URL(timblCard).openStream()
    val jmvCard = "http://jmvanel.free.fr/jmv.rdf"
    val triples = List(makeTriple(
        timsURI,
        foaf.knows,
        makeUri(jmvCard + "#me")))

    val store = makeRDFStore("tmpGraphStoreDir")
    val sirTim = makeTriple(
        makeUri("http://www.w3.org/People/Berners-Lee/card#i"),
        foaf.title,
        makeLiteral("Sir", xsd.string))

    val script =
      for {
        _ <- store.appendToGraph(makeUri("urn:foafs"), graph)
        _ <- store.appendToGraph(makeUri("urn:foafs"), Graph(triples))
        _ <- store.remove(makeUri("urn:foafs"), Seq(sirTim))
      } yield {
        println("Successfully stored triples in store")
      }
    script.getOrFail()
  }

    init onSuccess {
      case _ =>
        println("Successfully stored triples in store")
        store.getGraph(makeUri("urn:foafs")).onSuccess {
          case gr =>
            val graphAsString = RDFXMLWriter.asString(gr, base = timblCard) getOrElse sys.error("coudn't serialize the graph")
            println("Tim's FOAF with 2 modifications:\n" + graphAsString)
            store.shutdown
            println("Successfully shutdown store")
        }
    }
  }
}

import org.w3.banana.jena._
import com.hp.hpl.jena.query.Dataset

object GraphExampleWithJena extends GraphStoreExample[Jena, Dataset] {

  def makeRDFStore(file: String): Dataset = {
    import com.hp.hpl.jena.tdb.TDBFactory
    TDBFactory.createDataset(file)
  }

}

import org.w3.banana.sesame._
import org.openrdf.repository.RepositoryConnection

object GraphExampleWithSesame extends GraphStoreExample[Sesame, RepositoryConnection] {

  def makeRDFStore(file: String): RepositoryConnection = {
    val repo = new SailRepository(new MemoryStore)
    val tempDir = java.nio.file.Files.createTempDirectory("sesame-").resolve(file).toFile
    tempDir.mkdirs()
    tempDir.deleteOnExit()
    repo.setDataDir(tempDir)
    repo.initialize()
    repo.getConnection()
  }
}
