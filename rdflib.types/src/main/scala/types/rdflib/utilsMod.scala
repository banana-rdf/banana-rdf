package types.rdflib

import types.rdflib.anon.Fetcher
import types.rdflib.tfTypesMod.Quad
import types.rdflib.tfTypesMod.QuadGraph
import types.rdflib.tfTypesMod.QuadObject
import types.rdflib.tfTypesMod.QuadPredicate
import types.rdflib.tfTypesMod.QuadSubject
import types.rdflib.tfTypesMod.RdfJsDataFactory
import types.rdflib.tfTypesMod.Term
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object utilsMod {
  
  @JSImport("rdflib/lib/utils", JSImport.Namespace)
  @js.native
  val ^ : js.Any = js.native
  
  inline def AJARHandleNewTerm(kb: Fetcher, p: js.Any, requestedBy: js.Any): js.Any = (^.asInstanceOf[js.Dynamic].applyDynamic("AJAR_handleNewTerm")(kb.asInstanceOf[js.Any], p.asInstanceOf[js.Any], requestedBy.asInstanceOf[js.Any])).asInstanceOf[js.Any]
  
  inline def ArrayIndexOf(arr: js.Any, item: js.Any): Double = (^.asInstanceOf[js.Dynamic].applyDynamic("ArrayIndexOf")(arr.asInstanceOf[js.Any], item.asInstanceOf[js.Any])).asInstanceOf[Double]
  inline def ArrayIndexOf(arr: js.Any, item: js.Any, i: Double): Double = (^.asInstanceOf[js.Dynamic].applyDynamic("ArrayIndexOf")(arr.asInstanceOf[js.Any], item.asInstanceOf[js.Any], i.asInstanceOf[js.Any])).asInstanceOf[Double]
  
  @JSImport("rdflib/lib/utils", "appliedFactoryMethods")
  @js.native
  val appliedFactoryMethods: js.Array[String] = js.native
  
  inline def arrayToStatements(rdfFactory: RdfJsDataFactory, subject: QuadSubject, data: js.Array[Term]): js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]] = (^.asInstanceOf[js.Dynamic].applyDynamic("arrayToStatements")(rdfFactory.asInstanceOf[js.Any], subject.asInstanceOf[js.Any], data.asInstanceOf[js.Any])).asInstanceOf[js.Array[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]]]
}
