package org.w3.banana.examples

import org.w3.banana._
import org.w3.banana.diesel._
import java.net.URL

/* declare your dependencies as a trait with all the modules you need
 */
trait SPARQLExampleDependencies
  extends RDFModule
  with RDFOpsModule
  with SparqlOpsModule
  with SparqlHttpModule

/* Here is an example doing some IO. Read below to see what's
 * happening.
 * 
 * As you can see, we never use Jena nor Sesame directly. The binding
 * is done later by providing the module implementation you
 * want. Hopefully, you'll have the same results :-)
 * 
 * To run this example from sbt:
 *   project examples
 *   run-main org.w3.banana.examples.SPARQLExampleWithJena
 */
trait SPARQLExample extends SPARQLExampleDependencies { self =>

  import Ops._
  import SparqlOps._

  def main(args: Array[String]): Unit = {

    /* gets a SparqlEngine out of a Sparql endpoint */

    val client = SparqlHttp(new URL("http://dbpedia.org/sparql/"))

    /* creates a Sparql Select query */

    val query = SelectQuery("""
PREFIX ont: <http://dbpedia.org/ontology/>
SELECT DISTINCT ?language WHERE {
 ?language a ont:ProgrammingLanguage .
 ?language ont:influencedBy ?other .
 ?other ont:influencedBy ?language .
} LIMIT 100
""")

    /* executes the query */

    val answers: Rdf#Solutions = client.executeSelect(query).getOrFail()

    /* iterate through the solutions */

    val languages: Iterable[Rdf#URI] = answers.toIterable map { row =>
      /* row is an Rdf#Solution, we can get an Rdf#Node from the variable name */
      /* both the #Rdf#Node projection and the transformation to Rdf#URI can fail in the Try type, hense the flatMap */
      row("language").flatMap(_.as[Rdf#URI]) getOrElse sys.error("die")
    }

    println(languages.toList)
  }

}

/* Here is how you instantiate your modules. Note that this is using
 * the default module implementations in banana-rdf but nothing is
 * preventing you from implementing your own or re-using part of
 * them. */

import org.w3.banana.jena.JenaModule

object SPARQLExampleWithJena extends SPARQLExample with JenaModule
