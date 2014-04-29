package org.w3.banana.examples

import scala.concurrent.ExecutionContext.Implicits.global

import org.w3.banana.Command
import org.w3.banana.FOAFPrefix
import org.w3.banana.RDFStore

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
trait GraphStoreExample extends IOExampleDependencies {

  import Ops._

  /*abstract*/ def makeRDFStore( file:String ) : RDFStore[Rdf]
  
  def main(args: Array[String]): Unit = {

    /* reads TimBL's card in Turtle */

    val timblCard = "http://www.w3.org/People/Berners-Lee/card.ttl"
    val from = (new java.net.URL(timblCard)).openStream()
    // reading from a stream can fail so in real life, you would have to deal with the Try[Rdf#Graph]
    val graph: Rdf#Graph = TurtleReader.read(from, base = timblCard) getOrElse sys.error("couldn't read TimBL's card")

    val jmvCard = "http://jmvanel.free.fr/jmv.rdf"
    val foaf = FOAFPrefix(Ops)
    val triples = List( makeTriple(
        makeUri(timblCard + "#i"),
        foaf.knows,
        makeUri(jmvCard + "#me") ))

    val store = makeRDFStore( "tmpGraphStoreDir" )
    val init = store.execute {
      Command.append(makeUri("urn:foafs"), graph.toIterable)
      Command.append(makeUri("urn:foafs"), triples)
    }
    init onSuccess{ case _ => println("Successfully stored triples in store") }
  }

}

import org.w3.banana.jena.JenaModule
import org.w3.banana.jena.JenaStore
import org.w3.banana.sesame.SesameModule
import com.hp.hpl.jena.tdb.TDBFactory

object GraphExampleWithJena extends GraphStoreExample with JenaModule {
  def makeRDFStore(file: String): RDFStore[Rdf] = {
    JenaStore(TDBFactory.createDataset(file).asDatasetGraph())
  }
}

object GraphExampleWithSesame extends GraphStoreExample with SesameModule {
  def makeRDFStore(file: String): RDFStore[Rdf] = {
    ???
  }
}
