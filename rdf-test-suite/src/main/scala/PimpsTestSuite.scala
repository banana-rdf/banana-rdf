package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers

// TODO use generators
abstract class PimpsTestSuite[Rdf <: RDF](val ops: RDFOperations[Rdf])
extends WordSpec with MustMatchers {
  
  import ops._
  val xsd = XSDPrefix(ops)
  
  "a tuple of (s: Node, p: URI, o: Node)" must {
    
    val s = BNode("something")
    val p = URI("some:property")
    val o = TypedLiteral("some value")
    // if it compiles, it means that the implicit it really there
    val tuple = (s, p, o)
    
    "be seen as an RDF Triple with accessors" in {
      val triple: Rdf#Triple = tuple
      // here we're pimping the Triple type with new accessors
      triple.subject must equal (s)
      triple.predicate must equal (p)
      triple.objectt must equal (o)
    }
  }
  
  "a node" must {
    val node = URI("foo")
    "be foldable" in {
      node fold (
        { case URI(iriString) => "foo" must equal (iriString) },
        { bnode => sys.error("should not be here") },
        { literal => sys.error("should not be here") }
      )
    }
  }
  
  "a literal" must {
    val literal = TypedLiteral("my-string", URI("xsd:string"))
    "be foldable" in {
      literal fold (
        { case TypedLiteral(lexicalForm, _) => "my-string" must equal (lexicalForm) },
        { langLiteral => sys.error("should not be here") }
      )
    }
    "have a lexical form" in {
      literal.lexicalForm must equal ("my-string")
    }
  }
  
  "a Scala Int" must {
    val i = 42
    "be mapped to TypedLiteral with the xsd:integer datatype" in {
      val lit: Rdf#TypedLiteral = 42
      val TypedLiteral(lexicalForm, datatype) = lit
      lexicalForm must equal ("42")
      datatype must equal (xsd.integer)
    }
  }
  
}

