package org.w3.banana.examples

import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.w3.banana._
import org.w3.banana.io._

import scala.util.Try

/* Here is an example storing in a local RDF store:
 * - triples computed
 * - triples read from Internet
 * 
 * See general explanations in IOExample.scala.
 * 
 * To run this example from sbt:
 *   project examples
 *   run-main org.w3.banana.examples.GraphExampleWithJena
 *   run-main org.w3.banana.examples.GraphExampleWithSesame
 */
abstract class GraphStoreExample[Rdf <: RDF, Store](implicit
  ops: RDFOps[Rdf],
  turtleReader: RDFReader[Rdf, Try, Turtle],
  rdfXMLWriter: RDFWriter[Rdf, Try, RDFXML],
  rdfStore: RDFStore[Rdf, Try, Store]
) {

  import ops._
  import rdfStore.graphStoreSyntax._

  def makeRDFStore(file: String): Store

  def main(args: Array[String]): Unit = {

    /* reads TimBL's card in Turtle */

    val timblCard = "http://www.w3.org/People/Berners-Lee/card.ttl"
    val from = new java.net.URL(timblCard).openStream()
    // reading from a stream can fail so in real life, you would have to deal with the Try[Rdf#Graph]
    val graph: Rdf#Graph = turtleReader.read(from, base = timblCard) getOrElse sys.error("couldn't read TimBL's card")

    val jmvCard = "http://jmvanel.free.fr/jmv.rdf"
    val foaf = FOAFPrefix[Rdf]
    val triples = List(makeTriple(
      makeUri(timblCard + "#i"),
      foaf.knows,
      makeUri(jmvCard + "#me")
    ))

    val store = makeRDFStore("tmpGraphStoreDir")
    val script =
      for {
        _ <- store
          .appendToGraph(makeUri("urn:foafs"), graph)
        _ <- store.appendToGraph(makeUri("urn:foafs"), Graph(triples))
      } yield {
        println("Successfully stored triples in store")
      }
    script.get
  }

}

import org.apache.jena.query.Dataset
import org.w3.banana.jena._

object GraphExampleWithJena extends GraphStoreExample[Jena, Dataset] {

  def makeRDFStore(file: String): Dataset = {
    import org.apache.jena.tdb.TDBFactory
    TDBFactory.createDataset(file)
  }

}

import org.openrdf.repository.RepositoryConnection
import org.w3.banana.sesame._

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
