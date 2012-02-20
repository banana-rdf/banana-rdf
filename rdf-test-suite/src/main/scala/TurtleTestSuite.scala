package org.w3.rdf

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import java.io._
import org.scalatest.EitherValues._

abstract class TurtleTestSuite[M <: RDFModule](val m: M, reader: TurtleReader[M], iso: GraphIsomorphism[M]) extends WordSpec with MustMatchers {
  
  import m._

  "read TURTLE version of timbl's card" in {
    val file = new File("rdf-test-suite/src/main/resources/card.ttl")
    val graph = reader.read(file, file.getAbsolutePath)
    graph.right.value must have size (77)
  }
  
}
