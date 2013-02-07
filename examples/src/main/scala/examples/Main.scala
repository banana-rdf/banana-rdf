package org.w3.banana.examples

/** examples for doing IO
  * from sbt:
  *   project examples
  *   run-main org.w3.banana.examples.IOExample
  */
object IOExample {

  import org.w3.banana._
  import scala.util.Properties
  import java.io.File
  import scalax.io.{ OutputResource, Resource }

  // just because we can :-)
  implicit class FileW(val file: File) extends AnyVal {
    def /(child: String): File = new File(file, child)
  }

  def main(args: Array[String]): Unit = {
    /* gets the important modules
     * you could use directly the ones declared in the package object
     */

    // RDFOps holds all the operations to manipulate RDF graphs, plus some syntax constructs
    val ops = RDFOps[Rdf]
    import ops._

    // gets Turtle reader
    val reader = RDFReader[Rdf, Turtle]

    // gets RDF/XML writer
    val writer = RDFWriter[Rdf, RDFXML]

    /* reads TimBL's card in Turtle */

    val timblCard = "http://www.w3.org/People/Berners-Lee/card.ttl"
    // note: as we're using scalax.io, we don't need to worry about closing the streams
    val from = Resource.fromURL(timblCard)
    // reading from a stream can fail so in real life, you would have to deal with the Try[Rdf#Graph]
    val graph: Rdf#Graph = reader.read(from, base = timblCard) getOrElse sys.error("couldn't read TimBL's card")
    
    /* prints TimBL's card to a file as RDF/XML */

    val tmpFile = new File(Properties.tmpDir) / "card.ttl"
    val to = Resource.fromFile(tmpFile)
    val ret = writer.write(graph, to, base = timblCard)
    if (ret.isSuccess)
      println(s"successfuly wrote TimBL's card to ${tmpFile.getAbsolutePath}")

    /* prints 10 triples to stdout */

    val graph10Triples = Graph(graph.toIterable.take(10).toSet)
    val graphAsString = writer.asString(graph10Triples, base = timblCard) getOrElse sys.error("coudn't serialize the graph")
    println(graphAsString)
  }

}
