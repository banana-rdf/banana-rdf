package org.w3.banana

import java.net.URL

import org.w3.banana.RDF.*

import scala.util.Try

open class SparqlEngineSyntaxTest[Rdf <: RDF](using
    ops: Ops[Rdf]
) extends munit.FunSuite:
   import ops.{given, *}

   test("parsing valid SPARQL select as SPARQL select") {
     checkSelect(
       """
         SELECT DISTINCT ?language
         WHERE {
           ?language a ont:ProgrammingLanguage .
         }
         LIMIT 100
       """,
       Prefix("ont", "http://dbpedia.org/ontology/")
     )
   }

   test("parsing SPARQL select with syntax errors".fail) {
     checkSelect(
       """
         SELECT DISTINCT ?language
         WHERE
           language a ont:ProgrammingLanguage .
         }
         LIMIT 100
       """,
       Prefix("ont", "http://dbpedia.org/ontology/")
     )
   }

   test("parsing valid SPARQL construct as SPARQL select".fail) {
     checkSelect(
       """
         CONSTRUCT {
           res:Haskell a ont:ProgrammingLanguage .
         }
         WHERE {
           ?language a ont:ProgrammingLanguage .
         }
         LIMIT 100
       """,
       Prefix("ont", "http://dbpedia.org/ontology/"),
       Prefix("res", "http://dbpedia.org/resource/")
     )
   }

   test("parsing valid SPARQL construct as SPARQL construct") {
     checkConstruct(
       """
         CONSTRUCT {
           res:Haskell a ont:ProgrammingLanguage .
         }
         WHERE {
           ?language a ont:ProgrammingLanguage .
         }
         LIMIT 100
       """,
       Prefix("ont", "http://dbpedia.org/ontology/"),
       Prefix("res", "http://dbpedia.org/resource/")
     )
   }

   test("parsing valid SPARQL construct with syntax errors".fail) {
     checkConstruct(
       """
         CONSTRUCT
           res:Haskell a ont:ProgrammingLanguage .
         }
         WHERE
           language a ont:ProgrammingLanguage .
         }
         LIMT 100
       """,
       Prefix("ont", "http://dbpedia.org/ontology/"),
       Prefix("res", "http://dbpedia.org/resource/")
     )
   }

   test("parsing valid SPARQL select as SPARQL construct".fail) {
     checkConstruct(
       """
         SELECT DISTINCT ?language
         WHERE {
           ?language a ont:ProgrammingLanguage .
         }
         LIMIT 100
       """,
       Prefix("ont", "http://dbpedia.org/ontology/")
     )
   }

   test("parsing valid SPARQL ask as SPARQL ask") {
     checkAsk(
       """
         ASK {
           res:Haskell a ont:ProgrammingLanguage .
         }
       """,
       Prefix("ont", "http://dbpedia.org/ontology/"),
       Prefix("res", "http://dbpedia.org/resource/")
     )
   }

   test("parsing valid SPARQL ask with syntax errors".fail) {
     checkAsk(
       """
         ASK
           res::Haskell a ont:ProgrammingLanguage .
         }
       """,
       Prefix("ont", "http://dbpedia.org/ontology/"),
       Prefix("res", "http://dbpedia.org/resource/")
     )
   }

   test("parsing valid SPARQL select as SPARQL ask".fail) {
     checkAsk(
       """
         SELECT DISTINCT ?language
         WHERE {
           ?language a ont:ProgrammingLanguage .
         }
         LIMIT 100
       """,
       Prefix("ont", "http://dbpedia.org/ontology/")
     )
   }

   def checkSelect(query: String, prefixes: Prefix[Rdf]*) =
     assertSuccess(query.asSelect(prefixes))

   def checkConstruct(query: String, prefixes: Prefix[Rdf]*) =
     assertSuccess(query.asConstruct(prefixes))

   def checkAsk(query: String, prefixes: Prefix[Rdf]*) =
     assertSuccess(query.asAsk(prefixes))

   def assertSuccess(result: Try[?]) =
     assert(result.isSuccess)
end SparqlEngineSyntaxTest
