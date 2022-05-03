package org.w3.banana

import java.net.URL

import cats.implicits.given

import org.w3.banana.RDF.*
import org.w3.banana.operations.SparqlEngine

import scala.util.Try
import scala.util.Success

open class SparqlEngineTest[Rdf <: RDF, A](store: A)(using
    ops: Ops[Rdf],
    engine: SparqlEngine[Rdf, Try, A]
) extends munit.FunSuite:
   import ops.{given, *}

   test("new-tr.rdf must have Alexandre Bertails as an editor") {
     val query =
       """
         |prefix : <http://www.w3.org/2001/02pd/rec54#>
         |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
         |prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>
         |
         |SELECT DISTINCT ?name WHERE {
         |  graph <http://example.com/graph> {
         |    ?thing :editor ?ed .
         |    ?ed contact:fullName ?name
         |  }
         |}""".stripMargin

     assertSuccess(
       for
          query     <- query.asSelect(Nil)
          solutions <- store.executeSelect(query)
          names <-
            solutions
              .iterator
              .map(_("name"))
              .toSeq
              .sequence
       yield names.exists {
         _.fold(_ => false, _ => false, lit => lit.text == "Alexandre Bertails")
       }
     )
   }

   def assertSuccess(result: Try[Boolean]) =
     assertEquals(result, Success(true))
end SparqlEngineTest
