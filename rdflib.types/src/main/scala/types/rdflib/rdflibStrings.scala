package types.rdflib

import types.rdflib.fetcherMod.HTTPMethods
import types.rdflib.fetcherMod._StatusValues
import types.rdflib.typesMod.ContentType
import types.rdflib.typesMod.TermType
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object rdflibStrings {
  
  @js.native
  sealed trait BlankNode
    extends StObject
       with TermType
  inline def BlankNode: BlankNode = "BlankNode".asInstanceOf[BlankNode]
  
  @js.native
  sealed trait CONNECT
    extends StObject
       with HTTPMethods
  inline def CONNECT: CONNECT = "CONNECT".asInstanceOf[CONNECT]
  
  @js.native
  sealed trait Collection
    extends StObject
       with TermType
  inline def Collection: Collection = "Collection".asInstanceOf[Collection]
  
  @js.native
  sealed trait DELETE
    extends StObject
       with HTTPMethods
  inline def DELETE: DELETE = "DELETE".asInstanceOf[DELETE]
  
  @js.native
  sealed trait DefaultGraph
    extends StObject
       with TermType
  inline def DefaultGraph: DefaultGraph = "DefaultGraph".asInstanceOf[DefaultGraph]
  
  @js.native
  sealed trait Empty
    extends StObject
       with TermType
  inline def Empty: Empty = "Empty".asInstanceOf[Empty]
  
  @js.native
  sealed trait FunctionalProperty extends StObject
  inline def FunctionalProperty: FunctionalProperty = "FunctionalProperty".asInstanceOf[FunctionalProperty]
  
  @js.native
  sealed trait GET
    extends StObject
       with HTTPMethods
  inline def GET: GET = "GET".asInstanceOf[GET]
  
  @js.native
  sealed trait Graph
    extends StObject
       with TermType
  inline def Graph: Graph = "Graph".asInstanceOf[Graph]
  
  @js.native
  sealed trait HEAD
    extends StObject
       with HTTPMethods
  inline def HEAD: HEAD = "HEAD".asInstanceOf[HEAD]
  
  @js.native
  sealed trait InverseFunctionalProperty extends StObject
  inline def InverseFunctionalProperty: InverseFunctionalProperty = "InverseFunctionalProperty".asInstanceOf[InverseFunctionalProperty]
  
  @js.native
  sealed trait Literal
    extends StObject
       with TermType
  inline def Literal: Literal = "Literal".asInstanceOf[Literal]
  
  @js.native
  sealed trait NamedNode
    extends StObject
       with TermType
  inline def NamedNode: NamedNode = "NamedNode".asInstanceOf[NamedNode]
  
  @js.native
  sealed trait OPTIONS
    extends StObject
       with HTTPMethods
  inline def OPTIONS: OPTIONS = "OPTIONS".asInstanceOf[OPTIONS]
  
  @js.native
  sealed trait PATCH
    extends StObject
       with HTTPMethods
  inline def PATCH: PATCH = "PATCH".asInstanceOf[PATCH]
  
  @js.native
  sealed trait POST
    extends StObject
       with HTTPMethods
  inline def POST: POST = "POST".asInstanceOf[POST]
  
  @js.native
  sealed trait PUT
    extends StObject
       with HTTPMethods
  inline def PUT: PUT = "PUT".asInstanceOf[PUT]
  
  @js.native
  sealed trait TRACE
    extends StObject
       with HTTPMethods
  inline def TRACE: TRACE = "TRACE".asInstanceOf[TRACE]
  
  @js.native
  sealed trait Variable
    extends StObject
       with TermType
  inline def Variable: Variable = "Variable".asInstanceOf[Variable]
  
  @js.native
  sealed trait _Colon extends StObject
  inline def _Colon: _Colon = "_:".asInstanceOf[_Colon]
  
  @js.native
  sealed trait _empty extends StObject
  inline def _empty: _empty = "".asInstanceOf[_empty]
  
  @js.native
  sealed trait applicationSlashldPlussignjson
    extends StObject
       with ContentType
  inline def applicationSlashldPlussignjson: applicationSlashldPlussignjson = "application/ld+json".asInstanceOf[applicationSlashldPlussignjson]
  
  @js.native
  sealed trait `applicationSlashn-quads`
    extends StObject
       with ContentType
  inline def `applicationSlashn-quads`: `applicationSlashn-quads` = "application/n-quads".asInstanceOf[`applicationSlashn-quads`]
  
  @js.native
  sealed trait `applicationSlashn-triples` extends StObject
  inline def `applicationSlashn-triples`: `applicationSlashn-triples` = "application/n-triples".asInstanceOf[`applicationSlashn-triples`]
  
  @js.native
  sealed trait applicationSlashn3
    extends StObject
       with ContentType
  inline def applicationSlashn3: applicationSlashn3 = "application/n3".asInstanceOf[applicationSlashn3]
  
  @js.native
  sealed trait applicationSlashnquads
    extends StObject
       with ContentType
  inline def applicationSlashnquads: applicationSlashnquads = "application/nquads".asInstanceOf[applicationSlashnquads]
  
  @js.native
  sealed trait applicationSlashrdfPlussignxml
    extends StObject
       with ContentType
  inline def applicationSlashrdfPlussignxml: applicationSlashrdfPlussignxml = "application/rdf+xml".asInstanceOf[applicationSlashrdfPlussignxml]
  
  @js.native
  sealed trait `applicationSlashsparql-update`
    extends StObject
       with ContentType
  inline def `applicationSlashsparql-update`: `applicationSlashsparql-update` = "application/sparql-update".asInstanceOf[`applicationSlashsparql-update`]
  
  @js.native
  sealed trait `applicationSlashsparql-update-single-match`
    extends StObject
       with ContentType
  inline def `applicationSlashsparql-update-single-match`: `applicationSlashsparql-update-single-match` = "application/sparql-update-single-match".asInstanceOf[`applicationSlashsparql-update-single-match`]
  
  @js.native
  sealed trait `applicationSlashx-turtle`
    extends StObject
       with ContentType
  inline def `applicationSlashx-turtle`: `applicationSlashx-turtle` = "application/x-turtle".asInstanceOf[`applicationSlashx-turtle`]
  
  @js.native
  sealed trait applicationSlashxhtmlPlussignxml
    extends StObject
       with ContentType
  inline def applicationSlashxhtmlPlussignxml: applicationSlashxhtmlPlussignxml = "application/xhtml+xml".asInstanceOf[applicationSlashxhtmlPlussignxml]
  
  @js.native
  sealed trait delete_ extends StObject
  inline def delete_ : delete_ = "delete".asInstanceOf[delete_]
  
  @js.native
  sealed trait done
    extends StObject
       with _StatusValues
  inline def done: done = "done".asInstanceOf[done]
  
  @js.native
  sealed trait failed
    extends StObject
       with _StatusValues
  inline def failed: failed = "failed".asInstanceOf[failed]
  
  @js.native
  sealed trait include extends StObject
  inline def include: include = "include".asInstanceOf[include]
  
  @js.native
  sealed trait omit extends StObject
  inline def omit: omit = "omit".asInstanceOf[omit]
  
  @js.native
  sealed trait parse_error
    extends StObject
       with _StatusValues
  inline def parse_error: parse_error = "parse_error".asInstanceOf[parse_error]
  
  @js.native
  sealed trait redirected
    extends StObject
       with _StatusValues
  inline def redirected: redirected = "redirected".asInstanceOf[redirected]
  
  @js.native
  sealed trait sameAs extends StObject
  inline def sameAs: sameAs = "sameAs".asInstanceOf[sameAs]
  
  @js.native
  sealed trait textSlashhtml
    extends StObject
       with ContentType
  inline def textSlashhtml: textSlashhtml = "text/html".asInstanceOf[textSlashhtml]
  
  @js.native
  sealed trait textSlashn3
    extends StObject
       with ContentType
  inline def textSlashn3: textSlashn3 = "text/n3".asInstanceOf[textSlashn3]
  
  @js.native
  sealed trait textSlashturtle
    extends StObject
       with ContentType
  inline def textSlashturtle: textSlashturtle = "text/turtle".asInstanceOf[textSlashturtle]
  
  @js.native
  sealed trait timeout
    extends StObject
       with _StatusValues
  inline def timeout: timeout = "timeout".asInstanceOf[timeout]
  
  @js.native
  sealed trait `two-direction` extends StObject
  inline def `two-direction`: `two-direction` = "two-direction".asInstanceOf[`two-direction`]
  
  @js.native
  sealed trait unsupported_protocol
    extends StObject
       with _StatusValues
  inline def unsupported_protocol: unsupported_protocol = "unsupported_protocol".asInstanceOf[unsupported_protocol]
}
