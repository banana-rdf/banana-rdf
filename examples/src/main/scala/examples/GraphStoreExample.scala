package org.w3.banana.examples

import scala.concurrent.ExecutionContext.Implicits.global

import org.w3.banana.Command
import org.w3.banana.FOAFPrefix
import org.w3.banana.XSDPrefix
import org.w3.banana.RDFStore
//import org.apache.log4j.Logger

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
trait GraphStoreExample extends IOExampleDependencies {

  import Ops._

  /*abstract*/ def makeRDFStore(file: String): RDFStore[Rdf]

  def main(args: Array[String]): Unit = {

    /* reads TimBL's card in Turtle */

    val timblCard = "http://www.w3.org/People/Berners-Lee/card.ttl"
    val from = (new java.net.URL(timblCard)).openStream()
    val graph = TurtleReader.read(from, base = timblCard) getOrElse sys.error("couldn't read TimBL's card")

    val jmvCard = "http://jmvanel.free.fr/jmv.rdf"
    val foaf = FOAFPrefix(Ops)
    val xsd = XSDPrefix(Ops)
    val timsURI = makeUri(timblCard + "#i")
    val triples = List(makeTriple(
        timsURI,
        foaf.knows,
        makeUri(jmvCard + "#me")))

    val store = makeRDFStore("tmpGraphStoreDir")
    val sirTim = makeTriple(
        makeUri("http://www.w3.org/People/Berners-Lee/card#i"),
        foaf.title,
        makeLiteral("Sir", xsd.string))
    val graphURI = makeUri("urn:foafs")

    val init = store.execute {
      for {
        _ <- Command.append(graphURI, graph.toIterable)
        _ <- Command.append(graphURI, triples)
        _ <- Command.remove(graphURI, Seq(sirTim))
      } yield ()
    }

    init onSuccess {
      case _ =>
        println("Successfully stored triples in store")
        store.getGraph(graphURI).onSuccess {
          case gr =>
            val graphAsString = RDFXMLWriter.asString(gr, base = timblCard) getOrElse sys.error("coudn't serialize the graph")
            println("Tim's FOAF with 2 modifications:\n" + graphAsString)
            store.shutdown
            println("Successfully shutdown store")
        }
    }
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
