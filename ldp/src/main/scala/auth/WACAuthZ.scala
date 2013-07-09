package org.w3.banana.ldp.auth

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import java.util.regex.Pattern
import org.w3.banana._
import org.w3.banana.ldp.{LDPCommand, WebResource}
import play.api.libs.iteratee.{Enumeratee, Input, Iteratee, Enumerator}
import scala.Some
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * WACAuthZ groups methods to find the authorized WebIDs for a particular resource
 * using a simple implementation of WebAccessControl http://www.w3.org/wiki/WebAccessControl
 *
 * @param web object that makes it easy to access web resources (local or remote) asynchronously
 * @param ops
 * @tparam Rdf
 */
class WACAuthZ[Rdf<:RDF](web: WebResource[Rdf])(implicit ops: RDFOps[Rdf]) {

  import LDPCommand._
  import ops._
  import org.w3.banana.diesel._
  import org.w3.banana.syntax.GraphSyntax
  import org.w3.banana.syntax.LiteralSyntax._
  import org.w3.banana.syntax.URISyntax._
  import web.rww

  val foaf = FOAFPrefix[Rdf]
  val wac = WebACLPrefix[Rdf]
  val rdfs = RDFSPrefix[Rdf]
  /**
   * Returns a Script for authentication that looks in the metadata file for a resource
   * to see what agents have access to a given resource in a given manner, following
   * resources.
   *
   *
   * but is not tail rec because of flatMap
   * @param aclUri metadata
   * @param method the method of access asked for ( in wac ontology )
   * @param on the resource to which access is requested
   * @return a Free based recursive structure that will return a list of agents ( identified by WebIDs. )
   * */
  def getAuth(aclUri: Rdf#URI, method: Rdf#URI, on: Rdf#URI): Future[List[Rdf#URI]] =
    getAuthEnum(web~(aclUri),method,on)


  protected def getAuthEnum(acls: Enumerator[LinkedDataResource[Rdf]],
                            method: Rdf#URI, on: Rdf#URI): Future[List[Rdf#URI]] =
       acls.flatMap(ldr=>authzWebIDs(ldr,on,method)) |>>> Iteratee.fold[Rdf#URI,List[Rdf#URI]](Nil){case (list,uri)=>uri::list}

  /**
   * getAuth for a resource ( fetch metadata for it )
   * @param resource the resource to check authorization for
   * @param method the type of access requested
   * @return
   */
  def getAuthFor(resource: Rdf#URI,  method: Rdf#URI): Future[List[Rdf#URI]] =
    getAuthEnum(acl(resource),method,resource)

  /** retrieves the acl for a resource */
  def acl(uri: Rdf#URI): Enumerator[LinkedDataResource[Rdf]] = {
    val futureACL: Future[Option[LinkedDataResource[Rdf]]] = rww.execute {
      //todo: this code could be moved somewhere else see: Command.GET
      val docUri = uri.fragmentLess
      getMeta(docUri).flatMap { m =>
        val x: LDPCommand.Script[Rdf, Option[LinkedDataResource[Rdf]]] = m.acl match {
          case Some(uri) => getLDPR(uri).map { g =>
            Some(LinkedDataResource(uri, PointedGraph(uri, g)))
          }
          case None => `return`(None)
        }
        x
      }
    }
    //todo we require an execution context here - I imported a global one, but user choice may be better
    new Enumerator[LinkedDataResource[Rdf]] {
      def apply[A](i: Iteratee[LinkedDataResource[Rdf], A]): Future[Iteratee[LinkedDataResource[Rdf], A]] =
        futureACL.flatMap { aclOpt =>
          i.feed(aclOpt match {
            case Some(ldr) => Input.El(ldr)
            case None => Input.Empty
          })
        }
    }
  }


  /**
   * return the list of agents that are allowed access to the given resource
   * stop looking if everybody is authorized
   * @param acldr the graph which contains the acl rules
   * @param on the resoure on which access is being requested
   * @param method the type of access requested
   * @return A list of Agents with access ( sometimes just an Agent )
   **/
  protected
  def authzWebIDs(acldr: LinkedDataResource[Rdf], on: Rdf#URI, method: Rdf#URI): Enumerator[Rdf#URI]  = {
    val authDefs = Enumerator((PointedGraph(method,acldr.resource.graph)/-wac.mode).toSeq:_*)

    // filter those pointedGraphs that give access to the required resource
    val accessToResourceFilter = Enumeratee.filter[PointedGraph[Rdf]]{ a=>
      (a/wac.accessTo).exists( _.pointer == on) ||
        (a/wac.accessToClass).exists{ clazzPg =>
          (clazzPg/wac.regex).exists{ regexPg =>
            foldNode(regexPg.pointer)(
              uri=>false,
              bnode=>false,
              lit=>Try(Pattern.compile(lit.lexicalForm).matcher(on.toString).matches()).getOrElse(false))
          }
        }
    }
    val relevantAcls: Enumerator[PointedGraph[Rdf]] = authDefs.through(accessToResourceFilter)

    //follow the wac.agent relations add those that are not bnodes to a list
    val agents: Enumerator[List[Rdf#URI]] = relevantAcls.map[List[Rdf#URI]]{ pg=>
      val webids = (pg/wac.agent).collect { case PointedGraph(p,_) if isURI(p) => p.asInstanceOf[Rdf#URI]}
      webids.toList
     }

    val agentClassLDRs: Enumerator[LinkedDataResource[Rdf]] =
      relevantAcls.flatMap( pg => web.~>(LinkedDataResource(acldr.location,pg),wac.agentClass){_.pointer != foaf.Agent})

    val seeAlso: Enumerator[Rdf#URI] = for {
      ldr <-  web.~>(acldr,wac.include)()
      uri <- authzWebIDs(ldr, on, method)
    } yield {
      uri
    }

    val groupMembers: Enumerator[List[Rdf#URI]] = agentClassLDRs.map{ldr=>
      val webids = if (ldr.resource.pointer == foaf.Agent) Iterable(foaf.Agent) //todo <- here we can stop
                   else (ldr.resource / foaf.member).collect { case PointedGraph(p, _) if isURI(p) => p.asInstanceOf[Rdf#URI]}
      webids.toList
    }

    //todo: stop at the first discovery of a foaf:Agent?
    //todo: collapse all agents into one foaf:Agent

    (agents andThen groupMembers).flatMap(uris=>Enumerator(uris.toSeq: _*)) andThen seeAlso
  }




}

