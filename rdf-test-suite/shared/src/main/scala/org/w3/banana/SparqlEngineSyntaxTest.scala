package org.w3.banana

import java.net.URL

import org.w3.banana.RDF.*

import scala.util.Try

open class SparqlEngineSyntaxTest[Rdf <: RDF, A](store: A)(using
    ops: Ops[Rdf],
    engine: SparqlEngine[Rdf, Try, A]
) extends munit.FunSuite:
   import ops.{given, *}

   test("success when parsing valid SPARQL select as SPARQL select") {
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
       yield ()

     assert(result.isSuccess)
   }

   test("failure when parsing SPARQL select with syntax errors") {
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
       yield ()

     assert(result.isFailure)
   }

   test("failure when parsing valid SPARQL construct as SPARQL select") {
     val result =
       for
          query <-
            """
              CONSTRUCT {
                res:Haskell a ont:ProgrammingLanguage .
              }
              WHERE {
                ?language a ont:ProgrammingLanguage .
              }
              LIMIT 100
            """.asSelect(
              Seq(
                Prefix("ont", "http://dbpedia.org/ontology/"),
                Prefix("res", "http://dbpedia.org/resource/")
              )
            )
       yield ()

     assert(result.isFailure)
   }

   test("success when parsing valid SPARQL construct as SPARQL construct") {
     val result =
       for
          query <-
            """
                CONSTRUCT {
                  res:Haskell a ont:ProgrammingLanguage .
                }
                WHERE {
                  ?language a ont:ProgrammingLanguage .
                }
                LIMIT 100
              """.asConstruct(
              Seq(
                Prefix("ont", "http://dbpedia.org/ontology/"),
                Prefix("res", "http://dbpedia.org/resource/")
              )
            )
       yield ()

     assert(result.isSuccess)
   }

   test("failure when parsing valid SPARQL construct with syntax errors") {
     val result =
       for
          query <-
            """
                CONSTRUCT
                  res:Haskell a ont:ProgrammingLanguage .
                }
                WHERE
                  language a ont:ProgrammingLanguage .
                }
                LIMT 100
              """.asConstruct(
              Seq(
                Prefix("ont", "http://dbpedia.org/ontology/"),
                Prefix("res", "http://dbpedia.org/resource/")
              )
            )
       yield ()

     assert(result.isFailure)
   }

   test("failure when parsing valid SPARQL select as SPARQL construct") {
     val result =
       for
          query <-
            """
              SELECT DISTINCT ?language
              WHERE {
                ?language a ont:ProgrammingLanguage .
              }
              LIMIT 100
            """.asConstruct(Seq(Prefix("ont", "http://dbpedia.org/ontology/")))
       yield ()

     assert(result.isFailure)
   }

   test("success when parsing valid SPARQL ask as SPARQL ask") {
     val result =
       for
          query <-
            """
                ASK {
                  res:Haskell a ont:ProgrammingLanguage .
                }
              """.asAsk(
              Seq(
                Prefix("ont", "http://dbpedia.org/ontology/"),
                Prefix("res", "http://dbpedia.org/resource/")
              )
            )
       yield ()

     assert(result.isSuccess)
   }

   test("failure when parsing valid SPARQL ask with syntax errors") {
     val result =
       for
          query <-
            """
              ASK
                res::Haskell a ont:ProgrammingLanguage .
              }
            """.asAsk(
              Seq(
                Prefix("ont", "http://dbpedia.org/ontology/"),
                Prefix("res", "http://dbpedia.org/resource/")
              )
            )
       yield ()

     assert(result.isFailure)
   }

   test("failure when parsing valid SPARQL select as SPARQL ask") {
     val result =
       for
          query <-
            """
              SELECT DISTINCT ?language
              WHERE {
                ?language a ont:ProgrammingLanguage .
              }
              LIMIT 100
            """.asAsk(Seq(Prefix("ont", "http://dbpedia.org/ontology/")))
       yield ()

     assert(result.isFailure)
   }
