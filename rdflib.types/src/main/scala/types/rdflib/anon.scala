package types.rdflib

import org.scalajs.dom.experimental.AbortSignal
import org.scalajs.dom.experimental.ReferrerPolicy
import org.scalajs.dom.experimental.RequestCache
import org.scalajs.dom.experimental.RequestInit
import org.scalajs.dom.experimental.RequestMode
import org.scalajs.dom.experimental.RequestRedirect
import types.rdflib.fetcherMod.ExtendedResponse
import types.rdflib.fetcherMod.Fetch
import types.rdflib.fetcherMod.HTTPMethods
import types.rdflib.fetcherMod.Handler
import types.rdflib.rdflibStrings.include
import types.rdflib.rdflibStrings.omit
import types.rdflib.statementMod.default
import types.rdflib.tfTypesMod.BlankNode
import types.rdflib.tfTypesMod.NamedNode
import types.rdflib.tfTypesMod.QuadSubject
import types.rdflib.tfTypesMod.Term
import types.rdflib.typesMod.ContentType
import types.rdflib.typesMod.GraphType
import types.rdflib.typesMod.ObjectType
import types.rdflib.typesMod.PredicateType
import types.rdflib.typesMod.SubjectType
import types.std.HeadersInit
import types.std.RequestInfo
import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

object anon {
  
  trait Delete extends StObject {
    
    var delete: js.UndefOr[js.Array[default[SubjectType, PredicateType, ObjectType, GraphType]]] = js.undefined
    
    var patch: js.UndefOr[js.Array[default[SubjectType, PredicateType, ObjectType, GraphType]]] = js.undefined
    
    var where: js.UndefOr[js.Any] = js.undefined
  }
  object Delete {
    
    inline def apply(): Delete = {
      val __obj = js.Dynamic.literal()
      __obj.asInstanceOf[Delete]
    }
    
    extension [Self <: Delete](x: Self) {
      
      inline def setDelete(value: js.Array[default[SubjectType, PredicateType, ObjectType, GraphType]]): Self = StObject.set(x, "delete", value.asInstanceOf[js.Any])
      
      inline def setDeleteUndefined: Self = StObject.set(x, "delete", js.undefined)
      
      inline def setDeleteVarargs(value: (default[SubjectType, PredicateType, ObjectType, GraphType])*): Self = StObject.set(x, "delete", js.Array(value :_*))
      
      inline def setPatch(value: js.Array[default[SubjectType, PredicateType, ObjectType, GraphType]]): Self = StObject.set(x, "patch", value.asInstanceOf[js.Any])
      
      inline def setPatchUndefined: Self = StObject.set(x, "patch", js.undefined)
      
      inline def setPatchVarargs(value: (default[SubjectType, PredicateType, ObjectType, GraphType])*): Self = StObject.set(x, "patch", js.Array(value :_*))
      
      inline def setWhere(value: js.Any): Self = StObject.set(x, "where", value.asInstanceOf[js.Any])
      
      inline def setWhereUndefined: Self = StObject.set(x, "where", js.undefined)
    }
  }
  
  trait Fetcher extends StObject {
    
    var fetcher: types.rdflib.fetcherMod.default
  }
  object Fetcher {
    
    inline def apply(fetcher: types.rdflib.fetcherMod.default): Fetcher = {
      val __obj = js.Dynamic.literal(fetcher = fetcher.asInstanceOf[js.Any])
      __obj.asInstanceOf[Fetcher]
    }
    
    extension [Self <: Fetcher](x: Self) {
      
      inline def setFetcher(value: types.rdflib.fetcherMod.default): Self = StObject.set(x, "fetcher", value.asInstanceOf[js.Any])
    }
  }
  
  trait Flags extends StObject {
    
    /**
      * A string of letters, each of which set an options
      * e.g. `deinprstux`
      */
    var flags: String
  }
  object Flags {
    
    inline def apply(flags: String): Flags = {
      val __obj = js.Dynamic.literal(flags = flags.asInstanceOf[js.Any])
      __obj.asInstanceOf[Flags]
    }
    
    extension [Self <: Flags](x: Self) {
      
      inline def setFlags(value: String): Self = StObject.set(x, "flags", value.asInstanceOf[js.Any])
    }
  }
  
  trait Q extends StObject {
    
    var q: js.UndefOr[Double | String] = js.undefined
  }
  object Q {
    
    inline def apply(): Q = {
      val __obj = js.Dynamic.literal()
      __obj.asInstanceOf[Q]
    }
    
    extension [Self <: Q](x: Self) {
      
      inline def setQ(value: Double | String): Self = StObject.set(x, "q", value.asInstanceOf[js.Any])
      
      inline def setQUndefined: Self = StObject.set(x, "q", js.undefined)
    }
  }
  
  trait Setobject extends StObject {
    
    def set_object(obj: js.Any, callbackFunction: js.Any): Unit
    
    var statement: js.UndefOr[js.Array[Term]] = js.undefined
    
    var statementNT: js.UndefOr[String] = js.undefined
    
    var where: String
  }
  object Setobject {
    
    inline def apply(set_object: (js.Any, js.Any) => Unit, where: String): Setobject = {
      val __obj = js.Dynamic.literal(set_object = js.Any.fromFunction2(set_object), where = where.asInstanceOf[js.Any])
      __obj.asInstanceOf[Setobject]
    }
    
    extension [Self <: Setobject](x: Self) {
      
      inline def setSet_object(value: (js.Any, js.Any) => Unit): Self = StObject.set(x, "set_object", js.Any.fromFunction2(value))
      
      inline def setStatement(value: js.Array[Term]): Self = StObject.set(x, "statement", value.asInstanceOf[js.Any])
      
      inline def setStatementNT(value: String): Self = StObject.set(x, "statementNT", value.asInstanceOf[js.Any])
      
      inline def setStatementNTUndefined: Self = StObject.set(x, "statementNT", js.undefined)
      
      inline def setStatementUndefined: Self = StObject.set(x, "statement", js.undefined)
      
      inline def setStatementVarargs(value: Term*): Self = StObject.set(x, "statement", js.Array(value :_*))
      
      inline def setWhere(value: String): Self = StObject.set(x, "where", value.asInstanceOf[js.Any])
    }
  }
  
  trait TypeofHandler extends StObject {
    
    /* static member */
    var pattern: js.RegExp
  }
  object TypeofHandler {
    
    inline def apply(pattern: js.RegExp): TypeofHandler = {
      val __obj = js.Dynamic.literal(pattern = pattern.asInstanceOf[js.Any])
      __obj.asInstanceOf[TypeofHandler]
    }
    
    extension [Self <: TypeofHandler](x: Self) {
      
      inline def setPattern(value: js.RegExp): Self = StObject.set(x, "pattern", value.asInstanceOf[js.Any])
    }
  }
  
  /* Inlined {  req :rdflib.rdflib/lib/tf-types.BlankNode,   original :rdflib.rdflib/lib/tf-types.Quad_Subject} & rdflib.rdflib/lib/fetcher.Options */
  trait reqBlankNodeoriginalQuadS extends StObject {
    
    var actualProxyURI: js.UndefOr[String] = js.undefined
    
    var baseURI: js.UndefOr[String] = js.undefined
    
    var body: js.UndefOr[String] = js.undefined
    
    var cache: js.UndefOr[RequestCache] = js.undefined
    
    var clearPreviousData: js.UndefOr[Boolean] = js.undefined
    
    var contentType: js.UndefOr[String] = js.undefined
    
    var credentials: js.UndefOr[include | omit] = js.undefined
    
    var data: js.UndefOr[String] = js.undefined
    
    var fetch: js.UndefOr[Fetch] = js.undefined
    
    var force: js.UndefOr[Boolean] = js.undefined
    
    var forceContentType: js.UndefOr[ContentType] = js.undefined
    
    var handlers: js.UndefOr[js.Array[Handler]] = js.undefined
    
    var headers: js.UndefOr[HeadersInit] = js.undefined
    
    var integrity: js.UndefOr[String] = js.undefined
    
    var keepalive: js.UndefOr[Boolean] = js.undefined
    
    var method: js.UndefOr[HTTPMethods] = js.undefined
    
    var mode: js.UndefOr[RequestMode] = js.undefined
    
    var noMeta: js.UndefOr[Boolean] = js.undefined
    
    var noRDFa: js.UndefOr[Boolean] = js.undefined
    
    var original: QuadSubject & js.UndefOr[NamedNode]
    
    var proxyUsed: js.UndefOr[Boolean] = js.undefined
    
    var redirect: js.UndefOr[RequestRedirect] = js.undefined
    
    var referrer: js.UndefOr[String] = js.undefined
    
    var referrerPolicy: js.UndefOr[ReferrerPolicy] = js.undefined
    
    var referringTerm: js.UndefOr[NamedNode] = js.undefined
    
    var req: BlankNode & js.UndefOr[BlankNode]
    
    var requestedURI: js.UndefOr[String] = js.undefined
    
    var resource: js.UndefOr[QuadSubject] = js.undefined
    
    var retriedWithNoCredentials: js.UndefOr[Boolean] = js.undefined
    
    var signal: js.UndefOr[AbortSignal | Null] = js.undefined
    
    var timeout: js.UndefOr[Double] = js.undefined
    
    var window: js.UndefOr[js.Any] = js.undefined
    
    var withCredentials: js.UndefOr[Boolean] = js.undefined
  }
  object reqBlankNodeoriginalQuadS {
    
    inline def apply(original: QuadSubject & js.UndefOr[NamedNode], req: BlankNode & js.UndefOr[BlankNode]): reqBlankNodeoriginalQuadS = {
      val __obj = js.Dynamic.literal(original = original.asInstanceOf[js.Any], req = req.asInstanceOf[js.Any])
      __obj.asInstanceOf[reqBlankNodeoriginalQuadS]
    }
    
    extension [Self <: reqBlankNodeoriginalQuadS](x: Self) {
      
      inline def setActualProxyURI(value: String): Self = StObject.set(x, "actualProxyURI", value.asInstanceOf[js.Any])
      
      inline def setActualProxyURIUndefined: Self = StObject.set(x, "actualProxyURI", js.undefined)
      
      inline def setBaseURI(value: String): Self = StObject.set(x, "baseURI", value.asInstanceOf[js.Any])
      
      inline def setBaseURIUndefined: Self = StObject.set(x, "baseURI", js.undefined)
      
      inline def setBody(value: String): Self = StObject.set(x, "body", value.asInstanceOf[js.Any])
      
      inline def setBodyUndefined: Self = StObject.set(x, "body", js.undefined)
      
      inline def setCache(value: RequestCache): Self = StObject.set(x, "cache", value.asInstanceOf[js.Any])
      
      inline def setCacheUndefined: Self = StObject.set(x, "cache", js.undefined)
      
      inline def setClearPreviousData(value: Boolean): Self = StObject.set(x, "clearPreviousData", value.asInstanceOf[js.Any])
      
      inline def setClearPreviousDataUndefined: Self = StObject.set(x, "clearPreviousData", js.undefined)
      
      inline def setContentType(value: String): Self = StObject.set(x, "contentType", value.asInstanceOf[js.Any])
      
      inline def setContentTypeUndefined: Self = StObject.set(x, "contentType", js.undefined)
      
      inline def setCredentials(value: include | omit): Self = StObject.set(x, "credentials", value.asInstanceOf[js.Any])
      
      inline def setCredentialsUndefined: Self = StObject.set(x, "credentials", js.undefined)
      
      inline def setData(value: String): Self = StObject.set(x, "data", value.asInstanceOf[js.Any])
      
      inline def setDataUndefined: Self = StObject.set(x, "data", js.undefined)
      
      inline def setFetch(
        value: (/* input */ RequestInfo, /* init */ js.UndefOr[RequestInit]) => js.Promise[ExtendedResponse]
      ): Self = StObject.set(x, "fetch", js.Any.fromFunction2(value))
      
      inline def setFetchUndefined: Self = StObject.set(x, "fetch", js.undefined)
      
      inline def setForce(value: Boolean): Self = StObject.set(x, "force", value.asInstanceOf[js.Any])
      
      inline def setForceContentType(value: ContentType): Self = StObject.set(x, "forceContentType", value.asInstanceOf[js.Any])
      
      inline def setForceContentTypeUndefined: Self = StObject.set(x, "forceContentType", js.undefined)
      
      inline def setForceUndefined: Self = StObject.set(x, "force", js.undefined)
      
      inline def setHandlers(value: js.Array[Handler]): Self = StObject.set(x, "handlers", value.asInstanceOf[js.Any])
      
      inline def setHandlersUndefined: Self = StObject.set(x, "handlers", js.undefined)
      
      inline def setHandlersVarargs(value: Handler*): Self = StObject.set(x, "handlers", js.Array(value :_*))
      
      inline def setHeaders(value: HeadersInit): Self = StObject.set(x, "headers", value.asInstanceOf[js.Any])
      
      inline def setHeadersUndefined: Self = StObject.set(x, "headers", js.undefined)
      
      inline def setHeadersVarargs(value: js.Array[String]*): Self = StObject.set(x, "headers", js.Array(value :_*))
      
      inline def setIntegrity(value: String): Self = StObject.set(x, "integrity", value.asInstanceOf[js.Any])
      
      inline def setIntegrityUndefined: Self = StObject.set(x, "integrity", js.undefined)
      
      inline def setKeepalive(value: Boolean): Self = StObject.set(x, "keepalive", value.asInstanceOf[js.Any])
      
      inline def setKeepaliveUndefined: Self = StObject.set(x, "keepalive", js.undefined)
      
      inline def setMethod(value: HTTPMethods): Self = StObject.set(x, "method", value.asInstanceOf[js.Any])
      
      inline def setMethodUndefined: Self = StObject.set(x, "method", js.undefined)
      
      inline def setMode(value: RequestMode): Self = StObject.set(x, "mode", value.asInstanceOf[js.Any])
      
      inline def setModeUndefined: Self = StObject.set(x, "mode", js.undefined)
      
      inline def setNoMeta(value: Boolean): Self = StObject.set(x, "noMeta", value.asInstanceOf[js.Any])
      
      inline def setNoMetaUndefined: Self = StObject.set(x, "noMeta", js.undefined)
      
      inline def setNoRDFa(value: Boolean): Self = StObject.set(x, "noRDFa", value.asInstanceOf[js.Any])
      
      inline def setNoRDFaUndefined: Self = StObject.set(x, "noRDFa", js.undefined)
      
      inline def setOriginal(value: QuadSubject & js.UndefOr[NamedNode]): Self = StObject.set(x, "original", value.asInstanceOf[js.Any])
      
      inline def setProxyUsed(value: Boolean): Self = StObject.set(x, "proxyUsed", value.asInstanceOf[js.Any])
      
      inline def setProxyUsedUndefined: Self = StObject.set(x, "proxyUsed", js.undefined)
      
      inline def setRedirect(value: RequestRedirect): Self = StObject.set(x, "redirect", value.asInstanceOf[js.Any])
      
      inline def setRedirectUndefined: Self = StObject.set(x, "redirect", js.undefined)
      
      inline def setReferrer(value: String): Self = StObject.set(x, "referrer", value.asInstanceOf[js.Any])
      
      inline def setReferrerPolicy(value: ReferrerPolicy): Self = StObject.set(x, "referrerPolicy", value.asInstanceOf[js.Any])
      
      inline def setReferrerPolicyUndefined: Self = StObject.set(x, "referrerPolicy", js.undefined)
      
      inline def setReferrerUndefined: Self = StObject.set(x, "referrer", js.undefined)
      
      inline def setReferringTerm(value: NamedNode): Self = StObject.set(x, "referringTerm", value.asInstanceOf[js.Any])
      
      inline def setReferringTermUndefined: Self = StObject.set(x, "referringTerm", js.undefined)
      
      inline def setReq(value: BlankNode & js.UndefOr[BlankNode]): Self = StObject.set(x, "req", value.asInstanceOf[js.Any])
      
      inline def setRequestedURI(value: String): Self = StObject.set(x, "requestedURI", value.asInstanceOf[js.Any])
      
      inline def setRequestedURIUndefined: Self = StObject.set(x, "requestedURI", js.undefined)
      
      inline def setResource(value: QuadSubject): Self = StObject.set(x, "resource", value.asInstanceOf[js.Any])
      
      inline def setResourceUndefined: Self = StObject.set(x, "resource", js.undefined)
      
      inline def setRetriedWithNoCredentials(value: Boolean): Self = StObject.set(x, "retriedWithNoCredentials", value.asInstanceOf[js.Any])
      
      inline def setRetriedWithNoCredentialsUndefined: Self = StObject.set(x, "retriedWithNoCredentials", js.undefined)
      
      inline def setSignal(value: AbortSignal): Self = StObject.set(x, "signal", value.asInstanceOf[js.Any])
      
      inline def setSignalNull: Self = StObject.set(x, "signal", null)
      
      inline def setSignalUndefined: Self = StObject.set(x, "signal", js.undefined)
      
      inline def setTimeout(value: Double): Self = StObject.set(x, "timeout", value.asInstanceOf[js.Any])
      
      inline def setTimeoutUndefined: Self = StObject.set(x, "timeout", js.undefined)
      
      inline def setWindow(value: js.Any): Self = StObject.set(x, "window", value.asInstanceOf[js.Any])
      
      inline def setWindowUndefined: Self = StObject.set(x, "window", js.undefined)
      
      inline def setWithCredentials(value: Boolean): Self = StObject.set(x, "withCredentials", value.asInstanceOf[js.Any])
      
      inline def setWithCredentialsUndefined: Self = StObject.set(x, "withCredentials", js.undefined)
    }
  }
  
  /* Inlined {  req :rdflib.rdflib/lib/tf-types.BlankNode,   resource :rdflib.rdflib/lib/tf-types.Quad_Subject} & rdflib.rdflib/lib/fetcher.Options */
  trait reqBlankNoderesourceQuadS extends StObject {
    
    var actualProxyURI: js.UndefOr[String] = js.undefined
    
    var baseURI: js.UndefOr[String] = js.undefined
    
    var body: js.UndefOr[String] = js.undefined
    
    var cache: js.UndefOr[RequestCache] = js.undefined
    
    var clearPreviousData: js.UndefOr[Boolean] = js.undefined
    
    var contentType: js.UndefOr[String] = js.undefined
    
    var credentials: js.UndefOr[include | omit] = js.undefined
    
    var data: js.UndefOr[String] = js.undefined
    
    var fetch: js.UndefOr[Fetch] = js.undefined
    
    var force: js.UndefOr[Boolean] = js.undefined
    
    var forceContentType: js.UndefOr[ContentType] = js.undefined
    
    var handlers: js.UndefOr[js.Array[Handler]] = js.undefined
    
    var headers: js.UndefOr[HeadersInit] = js.undefined
    
    var integrity: js.UndefOr[String] = js.undefined
    
    var keepalive: js.UndefOr[Boolean] = js.undefined
    
    var method: js.UndefOr[HTTPMethods] = js.undefined
    
    var mode: js.UndefOr[RequestMode] = js.undefined
    
    var noMeta: js.UndefOr[Boolean] = js.undefined
    
    var noRDFa: js.UndefOr[Boolean] = js.undefined
    
    var original: js.UndefOr[NamedNode] = js.undefined
    
    var proxyUsed: js.UndefOr[Boolean] = js.undefined
    
    var redirect: js.UndefOr[RequestRedirect] = js.undefined
    
    var referrer: js.UndefOr[String] = js.undefined
    
    var referrerPolicy: js.UndefOr[ReferrerPolicy] = js.undefined
    
    var referringTerm: js.UndefOr[NamedNode] = js.undefined
    
    var req: BlankNode & js.UndefOr[BlankNode]
    
    var requestedURI: js.UndefOr[String] = js.undefined
    
    var resource: QuadSubject & js.UndefOr[QuadSubject]
    
    var retriedWithNoCredentials: js.UndefOr[Boolean] = js.undefined
    
    var signal: js.UndefOr[AbortSignal | Null] = js.undefined
    
    var timeout: js.UndefOr[Double] = js.undefined
    
    var window: js.UndefOr[js.Any] = js.undefined
    
    var withCredentials: js.UndefOr[Boolean] = js.undefined
  }
  object reqBlankNoderesourceQuadS {
    
    inline def apply(req: BlankNode & js.UndefOr[BlankNode], resource: QuadSubject & js.UndefOr[QuadSubject]): reqBlankNoderesourceQuadS = {
      val __obj = js.Dynamic.literal(req = req.asInstanceOf[js.Any], resource = resource.asInstanceOf[js.Any])
      __obj.asInstanceOf[reqBlankNoderesourceQuadS]
    }
    
    extension [Self <: reqBlankNoderesourceQuadS](x: Self) {
      
      inline def setActualProxyURI(value: String): Self = StObject.set(x, "actualProxyURI", value.asInstanceOf[js.Any])
      
      inline def setActualProxyURIUndefined: Self = StObject.set(x, "actualProxyURI", js.undefined)
      
      inline def setBaseURI(value: String): Self = StObject.set(x, "baseURI", value.asInstanceOf[js.Any])
      
      inline def setBaseURIUndefined: Self = StObject.set(x, "baseURI", js.undefined)
      
      inline def setBody(value: String): Self = StObject.set(x, "body", value.asInstanceOf[js.Any])
      
      inline def setBodyUndefined: Self = StObject.set(x, "body", js.undefined)
      
      inline def setCache(value: RequestCache): Self = StObject.set(x, "cache", value.asInstanceOf[js.Any])
      
      inline def setCacheUndefined: Self = StObject.set(x, "cache", js.undefined)
      
      inline def setClearPreviousData(value: Boolean): Self = StObject.set(x, "clearPreviousData", value.asInstanceOf[js.Any])
      
      inline def setClearPreviousDataUndefined: Self = StObject.set(x, "clearPreviousData", js.undefined)
      
      inline def setContentType(value: String): Self = StObject.set(x, "contentType", value.asInstanceOf[js.Any])
      
      inline def setContentTypeUndefined: Self = StObject.set(x, "contentType", js.undefined)
      
      inline def setCredentials(value: include | omit): Self = StObject.set(x, "credentials", value.asInstanceOf[js.Any])
      
      inline def setCredentialsUndefined: Self = StObject.set(x, "credentials", js.undefined)
      
      inline def setData(value: String): Self = StObject.set(x, "data", value.asInstanceOf[js.Any])
      
      inline def setDataUndefined: Self = StObject.set(x, "data", js.undefined)
      
      inline def setFetch(
        value: (/* input */ RequestInfo, /* init */ js.UndefOr[RequestInit]) => js.Promise[ExtendedResponse]
      ): Self = StObject.set(x, "fetch", js.Any.fromFunction2(value))
      
      inline def setFetchUndefined: Self = StObject.set(x, "fetch", js.undefined)
      
      inline def setForce(value: Boolean): Self = StObject.set(x, "force", value.asInstanceOf[js.Any])
      
      inline def setForceContentType(value: ContentType): Self = StObject.set(x, "forceContentType", value.asInstanceOf[js.Any])
      
      inline def setForceContentTypeUndefined: Self = StObject.set(x, "forceContentType", js.undefined)
      
      inline def setForceUndefined: Self = StObject.set(x, "force", js.undefined)
      
      inline def setHandlers(value: js.Array[Handler]): Self = StObject.set(x, "handlers", value.asInstanceOf[js.Any])
      
      inline def setHandlersUndefined: Self = StObject.set(x, "handlers", js.undefined)
      
      inline def setHandlersVarargs(value: Handler*): Self = StObject.set(x, "handlers", js.Array(value :_*))
      
      inline def setHeaders(value: HeadersInit): Self = StObject.set(x, "headers", value.asInstanceOf[js.Any])
      
      inline def setHeadersUndefined: Self = StObject.set(x, "headers", js.undefined)
      
      inline def setHeadersVarargs(value: js.Array[String]*): Self = StObject.set(x, "headers", js.Array(value :_*))
      
      inline def setIntegrity(value: String): Self = StObject.set(x, "integrity", value.asInstanceOf[js.Any])
      
      inline def setIntegrityUndefined: Self = StObject.set(x, "integrity", js.undefined)
      
      inline def setKeepalive(value: Boolean): Self = StObject.set(x, "keepalive", value.asInstanceOf[js.Any])
      
      inline def setKeepaliveUndefined: Self = StObject.set(x, "keepalive", js.undefined)
      
      inline def setMethod(value: HTTPMethods): Self = StObject.set(x, "method", value.asInstanceOf[js.Any])
      
      inline def setMethodUndefined: Self = StObject.set(x, "method", js.undefined)
      
      inline def setMode(value: RequestMode): Self = StObject.set(x, "mode", value.asInstanceOf[js.Any])
      
      inline def setModeUndefined: Self = StObject.set(x, "mode", js.undefined)
      
      inline def setNoMeta(value: Boolean): Self = StObject.set(x, "noMeta", value.asInstanceOf[js.Any])
      
      inline def setNoMetaUndefined: Self = StObject.set(x, "noMeta", js.undefined)
      
      inline def setNoRDFa(value: Boolean): Self = StObject.set(x, "noRDFa", value.asInstanceOf[js.Any])
      
      inline def setNoRDFaUndefined: Self = StObject.set(x, "noRDFa", js.undefined)
      
      inline def setOriginal(value: NamedNode): Self = StObject.set(x, "original", value.asInstanceOf[js.Any])
      
      inline def setOriginalUndefined: Self = StObject.set(x, "original", js.undefined)
      
      inline def setProxyUsed(value: Boolean): Self = StObject.set(x, "proxyUsed", value.asInstanceOf[js.Any])
      
      inline def setProxyUsedUndefined: Self = StObject.set(x, "proxyUsed", js.undefined)
      
      inline def setRedirect(value: RequestRedirect): Self = StObject.set(x, "redirect", value.asInstanceOf[js.Any])
      
      inline def setRedirectUndefined: Self = StObject.set(x, "redirect", js.undefined)
      
      inline def setReferrer(value: String): Self = StObject.set(x, "referrer", value.asInstanceOf[js.Any])
      
      inline def setReferrerPolicy(value: ReferrerPolicy): Self = StObject.set(x, "referrerPolicy", value.asInstanceOf[js.Any])
      
      inline def setReferrerPolicyUndefined: Self = StObject.set(x, "referrerPolicy", js.undefined)
      
      inline def setReferrerUndefined: Self = StObject.set(x, "referrer", js.undefined)
      
      inline def setReferringTerm(value: NamedNode): Self = StObject.set(x, "referringTerm", value.asInstanceOf[js.Any])
      
      inline def setReferringTermUndefined: Self = StObject.set(x, "referringTerm", js.undefined)
      
      inline def setReq(value: BlankNode & js.UndefOr[BlankNode]): Self = StObject.set(x, "req", value.asInstanceOf[js.Any])
      
      inline def setRequestedURI(value: String): Self = StObject.set(x, "requestedURI", value.asInstanceOf[js.Any])
      
      inline def setRequestedURIUndefined: Self = StObject.set(x, "requestedURI", js.undefined)
      
      inline def setResource(value: QuadSubject & js.UndefOr[QuadSubject]): Self = StObject.set(x, "resource", value.asInstanceOf[js.Any])
      
      inline def setRetriedWithNoCredentials(value: Boolean): Self = StObject.set(x, "retriedWithNoCredentials", value.asInstanceOf[js.Any])
      
      inline def setRetriedWithNoCredentialsUndefined: Self = StObject.set(x, "retriedWithNoCredentials", js.undefined)
      
      inline def setSignal(value: AbortSignal): Self = StObject.set(x, "signal", value.asInstanceOf[js.Any])
      
      inline def setSignalNull: Self = StObject.set(x, "signal", null)
      
      inline def setSignalUndefined: Self = StObject.set(x, "signal", js.undefined)
      
      inline def setTimeout(value: Double): Self = StObject.set(x, "timeout", value.asInstanceOf[js.Any])
      
      inline def setTimeoutUndefined: Self = StObject.set(x, "timeout", js.undefined)
      
      inline def setWindow(value: js.Any): Self = StObject.set(x, "window", value.asInstanceOf[js.Any])
      
      inline def setWindowUndefined: Self = StObject.set(x, "window", js.undefined)
      
      inline def setWithCredentials(value: Boolean): Self = StObject.set(x, "withCredentials", value.asInstanceOf[js.Any])
      
      inline def setWithCredentialsUndefined: Self = StObject.set(x, "withCredentials", js.undefined)
    }
  }
  
  /* Inlined {  req :rdflib.rdflib/lib/tf-types.Quad_Subject,   original :rdflib.rdflib/lib/tf-types.Quad_Subject} & rdflib.rdflib/lib/fetcher.Options */
  trait reqQuadSubjectoriginalQua extends StObject {
    
    var actualProxyURI: js.UndefOr[String] = js.undefined
    
    var baseURI: js.UndefOr[String] = js.undefined
    
    var body: js.UndefOr[String] = js.undefined
    
    var cache: js.UndefOr[RequestCache] = js.undefined
    
    var clearPreviousData: js.UndefOr[Boolean] = js.undefined
    
    var contentType: js.UndefOr[String] = js.undefined
    
    var credentials: js.UndefOr[include | omit] = js.undefined
    
    var data: js.UndefOr[String] = js.undefined
    
    var fetch: js.UndefOr[Fetch] = js.undefined
    
    var force: js.UndefOr[Boolean] = js.undefined
    
    var forceContentType: js.UndefOr[ContentType] = js.undefined
    
    var handlers: js.UndefOr[js.Array[Handler]] = js.undefined
    
    var headers: js.UndefOr[HeadersInit] = js.undefined
    
    var integrity: js.UndefOr[String] = js.undefined
    
    var keepalive: js.UndefOr[Boolean] = js.undefined
    
    var method: js.UndefOr[HTTPMethods] = js.undefined
    
    var mode: js.UndefOr[RequestMode] = js.undefined
    
    var noMeta: js.UndefOr[Boolean] = js.undefined
    
    var noRDFa: js.UndefOr[Boolean] = js.undefined
    
    var original: QuadSubject & js.UndefOr[NamedNode]
    
    var proxyUsed: js.UndefOr[Boolean] = js.undefined
    
    var redirect: js.UndefOr[RequestRedirect] = js.undefined
    
    var referrer: js.UndefOr[String] = js.undefined
    
    var referrerPolicy: js.UndefOr[ReferrerPolicy] = js.undefined
    
    var referringTerm: js.UndefOr[NamedNode] = js.undefined
    
    var req: QuadSubject & js.UndefOr[BlankNode]
    
    var requestedURI: js.UndefOr[String] = js.undefined
    
    var resource: js.UndefOr[QuadSubject] = js.undefined
    
    var retriedWithNoCredentials: js.UndefOr[Boolean] = js.undefined
    
    var signal: js.UndefOr[AbortSignal | Null] = js.undefined
    
    var timeout: js.UndefOr[Double] = js.undefined
    
    var window: js.UndefOr[js.Any] = js.undefined
    
    var withCredentials: js.UndefOr[Boolean] = js.undefined
  }
  object reqQuadSubjectoriginalQua {
    
    inline def apply(original: QuadSubject & js.UndefOr[NamedNode], req: QuadSubject & js.UndefOr[BlankNode]): reqQuadSubjectoriginalQua = {
      val __obj = js.Dynamic.literal(original = original.asInstanceOf[js.Any], req = req.asInstanceOf[js.Any])
      __obj.asInstanceOf[reqQuadSubjectoriginalQua]
    }
    
    extension [Self <: reqQuadSubjectoriginalQua](x: Self) {
      
      inline def setActualProxyURI(value: String): Self = StObject.set(x, "actualProxyURI", value.asInstanceOf[js.Any])
      
      inline def setActualProxyURIUndefined: Self = StObject.set(x, "actualProxyURI", js.undefined)
      
      inline def setBaseURI(value: String): Self = StObject.set(x, "baseURI", value.asInstanceOf[js.Any])
      
      inline def setBaseURIUndefined: Self = StObject.set(x, "baseURI", js.undefined)
      
      inline def setBody(value: String): Self = StObject.set(x, "body", value.asInstanceOf[js.Any])
      
      inline def setBodyUndefined: Self = StObject.set(x, "body", js.undefined)
      
      inline def setCache(value: RequestCache): Self = StObject.set(x, "cache", value.asInstanceOf[js.Any])
      
      inline def setCacheUndefined: Self = StObject.set(x, "cache", js.undefined)
      
      inline def setClearPreviousData(value: Boolean): Self = StObject.set(x, "clearPreviousData", value.asInstanceOf[js.Any])
      
      inline def setClearPreviousDataUndefined: Self = StObject.set(x, "clearPreviousData", js.undefined)
      
      inline def setContentType(value: String): Self = StObject.set(x, "contentType", value.asInstanceOf[js.Any])
      
      inline def setContentTypeUndefined: Self = StObject.set(x, "contentType", js.undefined)
      
      inline def setCredentials(value: include | omit): Self = StObject.set(x, "credentials", value.asInstanceOf[js.Any])
      
      inline def setCredentialsUndefined: Self = StObject.set(x, "credentials", js.undefined)
      
      inline def setData(value: String): Self = StObject.set(x, "data", value.asInstanceOf[js.Any])
      
      inline def setDataUndefined: Self = StObject.set(x, "data", js.undefined)
      
      inline def setFetch(
        value: (/* input */ RequestInfo, /* init */ js.UndefOr[RequestInit]) => js.Promise[ExtendedResponse]
      ): Self = StObject.set(x, "fetch", js.Any.fromFunction2(value))
      
      inline def setFetchUndefined: Self = StObject.set(x, "fetch", js.undefined)
      
      inline def setForce(value: Boolean): Self = StObject.set(x, "force", value.asInstanceOf[js.Any])
      
      inline def setForceContentType(value: ContentType): Self = StObject.set(x, "forceContentType", value.asInstanceOf[js.Any])
      
      inline def setForceContentTypeUndefined: Self = StObject.set(x, "forceContentType", js.undefined)
      
      inline def setForceUndefined: Self = StObject.set(x, "force", js.undefined)
      
      inline def setHandlers(value: js.Array[Handler]): Self = StObject.set(x, "handlers", value.asInstanceOf[js.Any])
      
      inline def setHandlersUndefined: Self = StObject.set(x, "handlers", js.undefined)
      
      inline def setHandlersVarargs(value: Handler*): Self = StObject.set(x, "handlers", js.Array(value :_*))
      
      inline def setHeaders(value: HeadersInit): Self = StObject.set(x, "headers", value.asInstanceOf[js.Any])
      
      inline def setHeadersUndefined: Self = StObject.set(x, "headers", js.undefined)
      
      inline def setHeadersVarargs(value: js.Array[String]*): Self = StObject.set(x, "headers", js.Array(value :_*))
      
      inline def setIntegrity(value: String): Self = StObject.set(x, "integrity", value.asInstanceOf[js.Any])
      
      inline def setIntegrityUndefined: Self = StObject.set(x, "integrity", js.undefined)
      
      inline def setKeepalive(value: Boolean): Self = StObject.set(x, "keepalive", value.asInstanceOf[js.Any])
      
      inline def setKeepaliveUndefined: Self = StObject.set(x, "keepalive", js.undefined)
      
      inline def setMethod(value: HTTPMethods): Self = StObject.set(x, "method", value.asInstanceOf[js.Any])
      
      inline def setMethodUndefined: Self = StObject.set(x, "method", js.undefined)
      
      inline def setMode(value: RequestMode): Self = StObject.set(x, "mode", value.asInstanceOf[js.Any])
      
      inline def setModeUndefined: Self = StObject.set(x, "mode", js.undefined)
      
      inline def setNoMeta(value: Boolean): Self = StObject.set(x, "noMeta", value.asInstanceOf[js.Any])
      
      inline def setNoMetaUndefined: Self = StObject.set(x, "noMeta", js.undefined)
      
      inline def setNoRDFa(value: Boolean): Self = StObject.set(x, "noRDFa", value.asInstanceOf[js.Any])
      
      inline def setNoRDFaUndefined: Self = StObject.set(x, "noRDFa", js.undefined)
      
      inline def setOriginal(value: QuadSubject & js.UndefOr[NamedNode]): Self = StObject.set(x, "original", value.asInstanceOf[js.Any])
      
      inline def setProxyUsed(value: Boolean): Self = StObject.set(x, "proxyUsed", value.asInstanceOf[js.Any])
      
      inline def setProxyUsedUndefined: Self = StObject.set(x, "proxyUsed", js.undefined)
      
      inline def setRedirect(value: RequestRedirect): Self = StObject.set(x, "redirect", value.asInstanceOf[js.Any])
      
      inline def setRedirectUndefined: Self = StObject.set(x, "redirect", js.undefined)
      
      inline def setReferrer(value: String): Self = StObject.set(x, "referrer", value.asInstanceOf[js.Any])
      
      inline def setReferrerPolicy(value: ReferrerPolicy): Self = StObject.set(x, "referrerPolicy", value.asInstanceOf[js.Any])
      
      inline def setReferrerPolicyUndefined: Self = StObject.set(x, "referrerPolicy", js.undefined)
      
      inline def setReferrerUndefined: Self = StObject.set(x, "referrer", js.undefined)
      
      inline def setReferringTerm(value: NamedNode): Self = StObject.set(x, "referringTerm", value.asInstanceOf[js.Any])
      
      inline def setReferringTermUndefined: Self = StObject.set(x, "referringTerm", js.undefined)
      
      inline def setReq(value: QuadSubject & js.UndefOr[BlankNode]): Self = StObject.set(x, "req", value.asInstanceOf[js.Any])
      
      inline def setRequestedURI(value: String): Self = StObject.set(x, "requestedURI", value.asInstanceOf[js.Any])
      
      inline def setRequestedURIUndefined: Self = StObject.set(x, "requestedURI", js.undefined)
      
      inline def setResource(value: QuadSubject): Self = StObject.set(x, "resource", value.asInstanceOf[js.Any])
      
      inline def setResourceUndefined: Self = StObject.set(x, "resource", js.undefined)
      
      inline def setRetriedWithNoCredentials(value: Boolean): Self = StObject.set(x, "retriedWithNoCredentials", value.asInstanceOf[js.Any])
      
      inline def setRetriedWithNoCredentialsUndefined: Self = StObject.set(x, "retriedWithNoCredentials", js.undefined)
      
      inline def setSignal(value: AbortSignal): Self = StObject.set(x, "signal", value.asInstanceOf[js.Any])
      
      inline def setSignalNull: Self = StObject.set(x, "signal", null)
      
      inline def setSignalUndefined: Self = StObject.set(x, "signal", js.undefined)
      
      inline def setTimeout(value: Double): Self = StObject.set(x, "timeout", value.asInstanceOf[js.Any])
      
      inline def setTimeoutUndefined: Self = StObject.set(x, "timeout", js.undefined)
      
      inline def setWindow(value: js.Any): Self = StObject.set(x, "window", value.asInstanceOf[js.Any])
      
      inline def setWindowUndefined: Self = StObject.set(x, "window", js.undefined)
      
      inline def setWithCredentials(value: Boolean): Self = StObject.set(x, "withCredentials", value.asInstanceOf[js.Any])
      
      inline def setWithCredentialsUndefined: Self = StObject.set(x, "withCredentials", js.undefined)
    }
  }
}
