package org.w3.banana

import org.w3.banana.RDF.*

import scala.util.Try

open class SparqlEngineTest[Rdf <: RDF, A](store: A)(using
    ops: Ops[Rdf],
    engine: SparqlEngine[Rdf, Try, A]
) extends munit.FunSuite:
   import ops.{given, *}

   test("haskell is influenced by another language") {
     isInfluencedBy(
       URI("http://dbpedia.org/resource/Haskell_(programming_language)")
     )
   }

   test("python is not influenced by another language".fail) {
     isInfluencedBy(
       URI("http://dbpedia.org/resource/Python_(programming_language)")
     )
   }

   def isInfluencedBy(uri: URI[Rdf]) =
      val containsLanguage =
        for
           query <-
             """
               SELECT DISTINCT ?language
               WHERE {
                 ?language a ont:ProgrammingLanguage .
                 ?language ont:influencedBy ?other .
               }
               LIMIT 100
             """.asSelect(Seq(Prefix("ont", "http://dbpedia.org/ontology/")))
           solutions <- store.executeSelect(query)
        yield solutions.iterator.exists { solution =>
          solution("language").map(node =>
            node.fold(
              _ == uri,
              _ => false,
              _ => false
            )
          ).getOrElse(false)
        }

      assert(containsLanguage.get)
