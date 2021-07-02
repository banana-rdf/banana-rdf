package org.w3.banana.io

import org.w3.banana.{RDF, RDFOps}
import scalaz.{Comonad, Monad}

abstract class RelativeTurtleTestSuite[Rdf <: RDF, M[+_] : Monad : Comonad](implicit
  ops: RDFOps[Rdf],
  reader: RDFReader[Rdf, M, Turtle],
  val writer: RDFWriter[Rdf, M, Turtle]
) extends RelativeGraphSerialisationTestSuite[Rdf, M, Turtle, Turtle]("Turtle", "ttl") {
  
}
