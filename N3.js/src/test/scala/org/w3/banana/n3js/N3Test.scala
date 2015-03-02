package org.w3.banana
package n3js

import zcheck.SpecLite
import scala.scalajs.js

import scalajs.concurrent.JSExecutionContext.Implicits.runNow

// the async stuff doesn't get properly tested by zcheck. Need to wait
// for scala-js 0.6. Look for [error] in the output in the meantime...
object N3Test extends SpecLite {

  // see https://github.com/RubenVerborgh/N3.js#from-rdf-chunks-to-triples
  "N3.Parser(): error" in {

    val parser = N3.Parser()

    val input =
"""
@prefix c: http://example.org/cartoons#>.
"""

    parser.parse(input)((t: Triple) => ()).failed.foreach { case ParsingError(_) => () }

  }

  // see https://github.com/RubenVerborgh/N3.js#from-an-rdf-document-to-triples
  "N3.Parser(): From an RDF document to triples " in {

    val parser = N3.Parser()

    var triples: Vector[Triple] = Vector.empty
    var prefixes: Map[String, String] = Map.empty

    val input =
"""
@prefix c: <http://example.org/cartoons#>.
c:Tom a c:Cat.
c:Jerry a c:Mouse;
        c:smarterThan c:Tom.
"""

    parser.parse(
      input)(
      (t: Triple) => triples :+= t,
      (k: String, v: String) => prefixes += (k -> v)
    ).foreach { _ =>
      check(triples.size == 3)

      val triple = triples.head

      check(triple.subject == "http://example.org/cartoons#Tom")
      check(triple.predicate == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
      check(triple.`object` == "http://example.org/cartoons#Cat")

      check(prefixes == Map("c" -> "http://example.org/cartoons#"))
    }

  }

  // see https://github.com/RubenVerborgh/N3.js#from-rdf-chunks-to-triples
  "N3.Parser(): From RDF chunks to triples" in {

    val parser = N3.Parser()

    var triples: Vector[Triple] = Vector.empty
    var prefixes: Map[String, String] = Map.empty

    val future = parser.parseChunks(
      (t: Triple) => triples :+= t,
      (k: String, v: String) => prefixes += (k -> v)
    )

    parser.addChunk("@prefix c: <http://example.org/cartoons#>.\n")
    parser.addChunk("c:Tom a ")
    parser.addChunk("c:Cat. c:Jerry a")
    check(triples.size == 1)
    parser.addChunk(" c:Mouse.")
    parser.end()

    check(triples.size == 2)

    future.foreach { _ =>

      check(triples.size == 2)

      val triple = triples.head

      check(triple.subject == "http://example.org/cartoons#Tom")
      check(triple.predicate == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
      check(triple.`object` == "http://example.org/cartoons#Cat")

    }


  }

  // see https://github.com/RubenVerborgh/N3.js#storing
  "N3.Store(): Storing" in {

    val store = N3.Store()

    store.addTriple("http://ex.org/Pluto",  "http://ex.org/type", "http://ex.org/Dog")
    store.addTriple("http://ex.org/Mickey", "http://ex.org/type", "http://ex.org/Mouse")

    val results = store.find("http://ex.org/Pluto", null, null)

    check(results.size == 1)

    val result = results(0)

    check(result.subject == "http://ex.org/Pluto")
    check(result.predicate == "http://ex.org/type")
    check(result.`object` == "http://ex.org/Dog")

    check(store.find(null, null, null).size == 2)

  }

  // see https://github.com/RubenVerborgh/N3.js#utility
  "N3.Util" in {

    val N3Util = N3.Util

    check(N3Util.isIRI("http://example.org/cartoons#Mickey"))

    check(N3Util.isLiteral(""""Mickey Mouse""""))
    check(N3Util.getLiteralValue(""""Mickey Mouse"""") == "Mickey Mouse")
    check(N3Util.isLiteral(""""Mickey Mouse"@en"""))
    check(N3Util.getLiteralLanguage(""""Mickey Mouse"@en""") == "en")
    check(N3Util.isLiteral(""""3"^^http://www.w3.org/2001/XMLSchema#integer"""))
    check(N3Util.getLiteralType(""""3"^^http://www.w3.org/2001/XMLSchema#integer""") == "http://www.w3.org/2001/XMLSchema#integer")
    check(N3Util.isLiteral(""""http://example.org/""""))
    check(N3Util.getLiteralValue(""""http://example.org/"""") == "http://example.org/")

    check(N3Util.isLiteral(""""This word is "quoted"!""""))
    check(N3Util.isLiteral(""""3"^^http://www.w3.org/2001/XMLSchema#integer"""))

    check(N3Util.isBlank("_:b1"))
    check(! N3Util.isIRI("_:b1"))
    check(! N3Util.isLiteral("_:b1"))

    val prefixes = js.JSON.parse("""{ "rdfs": "http://www.w3.org/2000/01/rdf-schema#" }""")
    check(N3Util.isPrefixedName("rdfs:label"))
    check(N3Util.expandPrefixedName("rdfs:label", prefixes) == "http://www.w3.org/2000/01/rdf-schema#label")

  }


}
