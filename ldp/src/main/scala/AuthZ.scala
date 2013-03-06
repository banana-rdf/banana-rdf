package org.w3.banana.ldp

import org.w3.banana._
import java.util.regex.Pattern
import scala.util.Try

/**
 * Authorization class
 *
 * This is an application of LDPCommand for authorization.
 * the main method is getAuth which returns a Script
 */
class AuthZ[Rdf<:RDF]( implicit ops: RDFOps[Rdf]) {
  import LDPCommand._
  import ops._
  import diesel._
  import syntax.GraphSyntax
  import syntax.LiteralSyntax._

  val foaf = FOAFPrefix[Rdf]
  val wac = WebACLPrefix[Rdf]

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
  def getAuth(aclUri: Rdf#URI, method: Rdf#URI, on: Rdf#URI, include: List[Agent] = List()): Script[Rdf, List[Agent]] =  {
    getLDPR(aclUri).flatMap { g: Rdf#Graph =>
      val az = authz(g, on, method)
      az match {
        case List(Agent) => `return`(az:::include)
        case agents => {
          //todo: we get the first, add support for more than one include...
          val inc= (PointedGraph(aclUri, g) / wac.include).collectFirst { //todo: check that it is in the collection. What to do if it's not?
            case PointedGraph(node,g) if isURI(node) =>
              getAuth(node.asInstanceOf[Rdf#URI],method,on,az)
          }
          val res = inc.getOrElse(`return`(az:::include))
          res
        }
      }
    }
  }

  /**
   * getAuth for a resource ( fetch metadata for it )
   * @param resource the resource to check authorization for
   * @param method the type of access requested
   * @param noMeta what should be assumed if no acl location is found?
   * @return
   */
  def getAuthFor(resource: Rdf#URI,  method: Rdf#URI, noMeta: List[Agent]=List()): Script[Rdf, List[Agent]] = {
    getMeta(resource).flatMap{meta =>
      meta.acl.map { acl => getAuth(acl, method, resource)}.getOrElse {
        `return`(noMeta)
      }
    }
  }


  /**
   * return the list of agents that are allowed access to the given resource
   * stop looking if everybody is authorized
   * @param aclGraph the graph which contains the acl rules
   * @param on the resoure on which access is being requested
   * @param method the type of access requested
   * @return A list of Agents with access ( sometimes just an Agent )
   **/
  protected
  def authz(aclGraph: Rdf#Graph, on: Rdf#URI, method: Rdf#URI): List[Agent]  = {
    def agent(a: PointedGraph[Rdf]): Agent = if (a.pointer == foaf.Agent) Agent
    else {
      val people = (a/foaf.member).collect{ case PointedGraph(p,_) if isURI(p)  => p.asInstanceOf[Rdf#URI]}.toList
      Group(people)
    }

    // does the authorization a give access to the resource?
    def filterOn(a: PointedGraph[Rdf]): Boolean = {
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
    def authorized(auths: Iterator[PointedGraph[Rdf]]): List[Agent] =
      if (!auths.hasNext) {
        List()
      } else {
        val az = auths.next
        if (!filterOn(az)) {
          authorized(auths)
        } else {
          val ac = (az / wac.agentClass).map { agent _ }.toList
//          val todo = (az / wac.agentClass).filter { pg =>
//            if (pg.pointer)   // I need the notion of a pointedNamedGraph, otherwise I cannot tell here if the URI is local or remote.
//          }.toList
          if (ac.contains(foaf.Agent))  List(Agent)  //we simplify
          else {
            (az/wac.agent).collect { case PointedGraph(p,_) if isURI(p) => p.asInstanceOf[Rdf#URI]}.toList match {
              case Nil => ac
              case list: List[Rdf#URI] => Group(list)::ac:::authorized(auths)
            }
          }
        }
      }

    val it = (PointedGraph(method,aclGraph)/-wac.mode)
    val result = authorized(it.iterator)
    //compress result
    if (result.contains(Agent)) List(Agent)
    else result
  }



  trait Agent {
   /**
    * @return true if the agent contain an agent referred to by the WebID. An Agent contains itself.
    **/
    def contains(id: Rdf#URI): Boolean
  }

  object Agent extends Agent {
    //the id should represent an Agent of course, and not something inanimate such as a stone, but we don't verify
    def contains(id: Rdf#URI) = true
    override def toString = "Agent(*)"
  }

  case class Group(members: List[Rdf#URI]) extends Agent {
    override
    def contains(id: Rdf#URI): Boolean = members.contains(id)
    override def toString = s"Agent($members)"
  }

  case class Person(id: Rdf#URI) extends Agent {
    override
    def contains(webid: Rdf#URI) = id == webid
    override def toString = s"Person($id)"
  }


}

