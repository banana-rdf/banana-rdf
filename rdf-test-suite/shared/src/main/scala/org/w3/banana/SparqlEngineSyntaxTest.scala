package org.w3.banana

import java.net.URL

import org.w3.banana.RDF.*

import scala.util.Try

open class SparqlEngineSyntaxTest[Rdf <: RDF, A](store: A)(using
    ops: Ops[Rdf],
    engine: SparqlEngine[Rdf, Try, A]
) extends munit.FunSuite:
   import ops.{given, *}

   test("valid SPARQL select is successfully executed") {
     val result =
       for
          query <-
            """
                SELECT DISTINCT ?language
                WHERE {
                  ?language a ont:ProgrammingLanguage .
                }
                LIMIT 100
              """.asSelect(Seq(Prefix("ont", "http://dbpedia.org/ontology/")))
          solutions <- store.executeSelect(query)
       yield ()

     assert(result.isSuccess)
   }

   test("invalid SPARQL select results in error") {
     val result =
       for
          query <-
            """
                SELECT DISTINCT ?language
                WHERE
                  language a ont:ProgrammingLanguage .
                }
                LIMIT 100
              """.asSelect(Seq(Prefix("ont", "http://dbpedia.org/ontology/")))
          solutions <- store.executeSelect(query)
       yield ()

     assert(result.isFailure)
   }
