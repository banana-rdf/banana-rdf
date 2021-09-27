package types.rdflib

import types.rdflib.tfTypesMod.BaseQuad
import types.rdflib.tfTypesMod.DefaultGraph
import types.rdflib.tfTypesMod.Quad
import types.rdflib.tfTypesMod.QuadGraph
import types.rdflib.tfTypesMod.QuadObject
import types.rdflib.tfTypesMod.QuadPredicate
import types.rdflib.tfTypesMod.QuadSubject
import types.rdflib.typesMod.Bindings
import types.rdflib.typesMod.GraphType
import types.rdflib.typesMod.ObjectType
import types.rdflib.typesMod.PredicateType
import types.rdflib.typesMod.SubjectType
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object statementMod {
  
  @JSImport("rdflib/lib/statement", JSImport.Default)
  @js.native
  class default[S /* <: SubjectType */, P /* <: PredicateType */, O /* <: ObjectType */, G /* <: GraphType */] protected ()
    extends StObject
       with Statement[S, P, O, G] {
    /**
      * Construct a new statement
      *
      * @param subject - The subject of the triple.  What the fact is about
      * @param predicate - The relationship which is asserted between the subject and object
      * @param object - The thing or data value which is asserted to be related to the subject
      * @param {NamedNode} graph - The document where the triple is or was or will be stored on the web.
      *
      * The graph param is a named node of the document in which the triple when it is stored
      *  on the web. It exists because when you have read data from various places in the web,
      *  the “graph” tells you _why_ you have the triple. (At the moment, it is just the
      *  document, in future it could be an inference step)
      *
      * When you do UpdateManager.update() then the graph’s of all the statements must be the same,
      *  and give the document you are patching. In future, we may have a more
      *  powerful update() which can update more than one document.
      */
    def this(subject: S, predicate: P, `object`: O) = this()
    def this(subject: S, predicate: P, `object`: O, graph: G) = this()
    def this(subject: S, predicate: P, `object`: O, graph: DefaultGraph) = this()
    
    /* CompleteClass */
    override def equals(other: BaseQuad): Boolean = js.native
    
    /* CompleteClass */
    var graph: G | DefaultGraph = js.native
    
    /* CompleteClass */
    var `object`: O = js.native
    
    /* CompleteClass */
    var predicate: P = js.native
    
    /* CompleteClass */
    var subject: S = js.native
  }
  
  @js.native
  trait Statement[S /* <: SubjectType */, P /* <: PredicateType */, O /* <: ObjectType */, G /* <: GraphType */]
    extends StObject
       with Quad[S, P, O, G | DefaultGraph] {
    
    /**
      * Checks whether two statements are the same
      * @param other - The other statement
      */
    def equals(other: Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]): Boolean = js.native
    
    /**
      * Creates a statement with the bindings substituted
      * @param bindings The bindings
      */
    def substitute(bindings: Bindings): Statement[SubjectType, PredicateType, ObjectType, GraphType] = js.native
    
    /** Creates a canonical string representation of this statement. */
    def toCanonical(): String = js.native
    
    /** Creates a n-quads string representation of this statement */
    def toNQ(): String = js.native
    
    /** Creates a n-triples string representation of this statement */
    def toNT(): String = js.native
    
    /** Alias for graph, favored by Tim */
    def why: DefaultGraph | G = js.native
    def why_=(g: DefaultGraph | G): Unit = js.native
  }
}
