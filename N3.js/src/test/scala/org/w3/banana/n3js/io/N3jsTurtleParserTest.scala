package org.w3.banana
package n3js
package io

import zcheck.SpecLite
import scalajs.concurrent.JSExecutionContext.Implicits.runNow
import org.w3.banana.plantain._

object N3jsTurtleParserTest extends SpecLite {

  import PlantainOps._

  val parser = new N3jsTurtleParser[Plantain]

  "Simple parsing" in {

    val sr = new java.io.StringReader("""
@prefix c: <http://example.org/cartoons#>.
c:Tom a c:Cat. 
c:Jerry a c:Mouse;
        c:smarterThan c:Tom.
""")

    parser.read(sr, "http://example.com").foreach { g =>
      val c = Prefix[Plantain]("c", "http://example.org/cartoons#")
      val graph = Graph(
        Triple(c("Tom"), rdf.typ, c("Cat")),
        Triple(c("Jerry"), rdf.typ, c("Mouse")),
        Triple(c("Jerry"), c("smarterThan"), c("Tom"))
      )
      check(g isIsomorphicWith graph)
    }

  }

}
