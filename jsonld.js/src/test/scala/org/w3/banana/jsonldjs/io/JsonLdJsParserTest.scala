package org.w3.banana
package jsonldjs
package io


import org.scalatest.{Matchers, WordSpec}

import scalajs.concurrent.JSExecutionContext.Implicits.queue
import org.w3.banana.plantain._

object JsonLdJsParserTest extends WordSpec with Matchers {

  import PlantainOps._

  val parser = new JsonLdJsParser[Plantain]

  "Simple parsing" in {

    val sr = new java.io.StringReader("""{
  "http://schema.org/name": "Manu Sporny",
  "http://schema.org/url": {"@id": "http://manu.sporny.org/"},
  "http://schema.org/image": {"@id": "http://manu.sporny.org/images/manu.png"}
}""")

    parser.read(sr, "http://example.com").foreach { g =>
      val schema = Prefix[Plantain]("schema", "http://schema.org/")
      // TODO why can't we get rid of that implicit?
      import org.w3.banana.binder.ToURI.URIToURI
      val graph = (
        BNode()
          -- schema("name") ->- "Manu Sporny"
          -- schema("url") ->- URI("http://manu.sporny.org/")
          -- schema("image") ->- URI("http://manu.sporny.org/images/manu.png")
      ).graph

      (g isIsomorphicWith graph) shouldEqual true
    }

  }

}
