package org.w3.banana.meta

import org.scalatest.{Matchers, WordSpec}
import org.w3.banana.{RDFOps, RDF}
import org.w3.banana.annotations.{Mappable, prop, vocab}
import scala.language.experimental.macros

/**
 * Tests how properties are extracted from class by RDF annotations
 * @param ops implicit RDF operations
 * @tparam Rdf RDF typeclass
 */
abstract class AnnotationTest[Rdf <: RDF](implicit ops:RDFOps[Rdf]) extends WordSpec with Matchers {


  def materialize(p:Person):Map[Rdf#URI,Any] //macroses are evaluated when they are called that is why I put them to final classes

  "extract RDF properties from classes" in {

    val p = new Person("Foo","Bar")
    val m = materialize(p)
    //not all rdf storess compare URIs in a right way (by string value) some compare by reference,
    // that is why I convert to string
    val keys = m.keys.map(_.toString)
    keys should contain (ops.makeUri("http://example.org/name").toString)
    keys should contain (ops.makeUri("http://example.org/surname").toString)
    keys should contain (ops.makeUri("http://example.org/mutant").toString)
    keys should contain (ops.makeUri("http://scala-lang.org/immutable").toString)


  }

}


