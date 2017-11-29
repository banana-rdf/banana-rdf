package org.w3.banana
package jsonldjs
package io

import org.scalatest.{AsyncWordSpec, Matchers}
import org.w3.banana.plantain._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.concurrent.ExecutionContext.Implicits.global

class JsonLdJsParserTest extends AsyncWordSpec with Matchers {

  implicit override def executionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  import PlantainOps._

  // TODO why can't we get rid of that implicit?
  import org.w3.banana.binder.ToURI.URIToURI

  val parser = new JsonLdJsParser[Plantain]

  val schema = Prefix[Plantain]("schema", "http://schema.org/")

  val srGraph = (
    BNode()
      -- schema("name") ->- "Manu Sporny"
      -- schema("url") ->- URI("http://manu.sporny.org/")
      -- schema("image") ->- URI("http://manu.sporny.org/images/manu.png")
    ).graph

  "Simple parsing" in {

    val sr = new java.io.StringReader("""{
  "@id": "http://www.example.org/Manu",
  "http://schema.org/name": "Manu Sporny",
  "http://schema.org/url": {"@id": "http://manu.sporny.org/"},
  "http://schema.org/image": {"@id": "http://manu.sporny.org/images/manu.png"}
}""")

    parser.read(sr, "http://example.com").map { g =>

      //(g isIsomorphicWith srGraph) shouldEqual true
      val subj = URI("http://www.example.org/Manu")

      g.size shouldEqual 3
      for(triple <- g.triples) println(triple.toString())
      g.contains( Triple(subj, schema("name"), Literal("Manu Sporny")) ) shouldEqual true
    }

  }

}
