package org.w3.banana.plantain

import org.w3.banana._

/**
 * Authorization class
 *
 * This is an application of LDPCommand for authorization.
 * the main method is getAuth which returns a Script
 */
class AuthZ[Rdf<:RDF]( implicit dsl: Diesel[Rdf]) {
  import org.w3.banana.plantain.LDPCommand._
  import dsl._
  import ops._

  val foaf = FOAFPrefix[Rdf]
  val wac = WebACL[Rdf]

  /**
   * Returns a Script for authentication that looks in metadata file for agents
   * that have access to a given resource in a given manner
   *
   * but is not tail rec because of flatMap
   * @param meta the meta data graph resource to look in
   * @param method the method of access asked for ( in wac ontology )
   * @return a Free based recursive structure
   * */
  def getAuth(meta: Rdf#URI, method: Rdf#URI): Script[Rdf, List[Agent]] =  {
    getLDPR(meta).flatMap { g: Rdf#Graph =>
      val az = authz(g, meta, method)
      az match {
        case List(Agent) => `return`(az)
        case agents => {
          val inc= (PointedGraph(URI(""), g) / wac.include).collectFirst { //todo: check that its' in the collection. What to do if it's not?
            case PointedGraph(node,g) if isURI(node) =>
              getAuth(node.asInstanceOf[Rdf#URI],method)
          }
          val res = inc.getOrElse(`return`(az))
          res
        }
      }
    }
  }

  /**
   * return the list of agents that are allowed access to the given resource
   * stop looking if everybody is authorized
   * @return A list of Agents with access ( should be perhaps just an Agent.
   **/
  protected
  def authz(acl: Rdf#Graph, resource: Rdf#URI, method: Rdf#URI): List[Agent]  = {
    def agent(a: PointedGraph[Rdf]): Agent = if (a.pointer == foaf.Agent) Agent
    else {
      val people = (a/foaf.member).collect{ case PointedGraph(p,_) if isURI(p)  => p.asInstanceOf[Rdf#URI]}.toList
      Group(people)
    }

    def authorized(auths: Iterator[PointedGraph[Rdf]]): List[Agent] =
      if (!auths.hasNext) {
        List()
      } else {
        val az = auths.next
        val ac = (az / wac.agentClass).map { agent _ }.toList
        if (ac.contains(foaf.Agent))  List(Agent)
        else {
          (az/wac.agent).collect { case PointedGraph(p,_) if isURI(p) => p.asInstanceOf[Rdf#URI]}.toList match {
            case Nil => ac
            case list: List[Rdf#URI] => Group(list)::ac:::authorized(auths)
          }
        }
      }

    val it = (PointedGraph(method,acl)/-wac.mode)
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
    //ok really the id should represent an Agent, and not say a stone
    def contains(id: Rdf#URI) = true
  }

  case class Group(members: List[Rdf#URI]) extends Agent {
    override
    def contains(id: Rdf#URI): Boolean = members.contains(id)
  }

  case class Person(id: Rdf#URI) extends Agent {
    override
    def contains(webid: Rdf#URI) = id == webid
  }


}

