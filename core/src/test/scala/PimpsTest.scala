package org.w3.rdf

import org.scalatest._
import org.scalatest.matchers.MustMatchers

abstract class PimpsTest[M <: Module](val m: M) extends WordSpec with MustMatchers {
  
  import m._
  
  val pimps: Pimps[m.type]
  import pimps._
  
  "a tuple of (s: Node, p: IRI, o: Node)" must {
    
    val s = BNode("something")
    val p = IRI("some:property")
    val o = TypedLiteral("some value")
    // if it compiles, it means that the implicit it really there
    val tuple = (s, p, o)
    
    "be seen as an RDF Triple with accessors" in {
      val triple: Triple = tuple
      // here we're pimping the Triple type with new accessors
      triple.subject must equal (s)
      triple.predicate must equal (p)
      triple.objectt must equal (o)
    }
  }
  
  "a node" must {
    val node = IRI("foo")
    "be foldable" in {
      node fold (
        { case IRI(iriString) => "foo" must equal (iriString) },
        { bnode => assert(false) },
        { literal => assert(false) }
      )
    }
  }
  
}

class PimpsForSimpleModuleTest extends PimpsTest(SimpleModule) {
  val pimps = PimpsForSimpleModule
}