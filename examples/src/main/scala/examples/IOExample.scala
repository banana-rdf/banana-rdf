package org.w3.banana.examples

import org.w3.banana._
import scala.util.Properties
import java.io.File

/* declare your dependencies as a trait with all the modules you need
 */
trait IOExampleDependencies
  extends RDFModule
  with RDFOpsModule
  with TurtleReaderModule
  with RDFXMLWriterModule

/* Here is an example doing some IO. Read below to see what's
 * happening.
 * 
 * As you can see, we never use Jena nor Sesame directly. The binding
 * is done later by providing the module implementation you
 * want. Hopefully, you'll have the same results :-)
 * 
 * To run this example from sbt:
 *   project examples
 *   run-main org.w3.banana.examples.IOExampleWithJena
 *   run-main org.w3.banana.examples.IOExampleWithSesame
 */
trait IOExample extends IOExampleDependencies {

  import ops._

  def main(args: Array[String]): Unit = {

    /* reads TimBL's card in Turtle */

    val timblCard = "http://www.w3.org/People/Berners-Lee/card.ttl"
    val from = new java.net.URL(timblCard).openStream()
    // reading from a stream can fail so in real life, you would have to deal with the Try[Rdf#Graph]
    val graph: Rdf#Graph = turtleReader.read(from, base = timblCard) getOrElse sys.error("couldn't read TimBL's card")

    /* prints TimBL's card to a file as RDF/XML */

    val tmpFile = new File(Properties.tmpDir, "card.ttl")
    val to = new java.io.FileOutputStream(tmpFile)
    val ret = rdfXMLWriter.write(graph, to, base = timblCard)
    if (ret.isSuccess)
      println(s"successfuly wrote TimBL's card to ${tmpFile.getAbsolutePath}")

    /* prints 10 triples to stdout */

    val graph10Triples = Graph(graph.triples.take(10).toSet)
    val graphAsString = rdfXMLWriter.asString(graph10Triples, base = timblCard) getOrElse sys.error("coudn't serialize the graph")
    println(graphAsString)
  }

}

/* Here is how you instantiate your modules. Note that this is using
 * the default module implementations in banana-rdf but nothing is
 * preventing you from implementing your own or re-using part of
 * them. */

import org.w3.banana.jena.JenaModule

object IOExampleWithJena extends IOExample with JenaModule

import org.w3.banana.sesame.SesameModule

object IOExampleWithSesame extends IOExample with SesameModule
