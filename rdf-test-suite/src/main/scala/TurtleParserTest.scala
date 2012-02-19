package org.w3.rdf

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import java.io._
import org.scalatest.EitherValues._

abstract class TurtleParserTest[M <: RDFModule](val m: M, parser: TurtleParser[M], iso: GraphIsomorphism[M]) extends WordSpec with MustMatchers {
  
  import m._

  "read TURTLE version of timbl's card" in {
    val file = new File("rdf-test-suite/src/main/resources/card.ttl")
    val graph = parser.read(file, file.getAbsolutePath)
    graph.right.value must have size (77)
  }
  
}
