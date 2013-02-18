import org.w3.banana._

/**
 * Authorization class
 *
 * This is an application of LDPCommand for authorization.
 * the main method is getAuth which returns a Script
 */
class AuthZ[Rdf<:RDF]( implicit ops: RDFOps[Rdf]) {
  import org.w3.banana.plantain.LDPCommand._
  import ops._

  val foaf = FOAFPrefix[Rdf]
  val wac = WebACLPrefix[Rdf]

  /**
   * Returns a Script for authentication that looks in the metadata file for a resource
   * to see what agents have access to a given resource in a given manner, following
   * resources.
   *
   *
   * but is not tail rec because of flatMap
   * @param meta metadata
   * @param method the method of access asked for ( in wac ontology )
   * @return a Free based recursive structure that will return a list of agents ( identified by WebIDs. )
   * */
  def getAuth(meta: Rdf#URI, method: Rdf#URI): Script[Rdf, List[Agent]] =  {
    getLDPR(meta).flatMap { g: Rdf#Graph =>
      val az = authz(g, meta, method)
      az match {
        case List(Agent) => `return`(az)
        case agents => {
          val inc= (PointedGraph(URI(""), g) / wac.include).collectFirst { //todo: check that it is in the collection. What to do if it's not?
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
   * getAuth for a resource ( fetch metadata for it )
   * @param resource the resource to check authorization for
   * @param method the type of access requested
   * @param noMeta what should be assumed if no acl location is found?
   * @return
   */
  def getAuthFor(resource: Rdf#URI,  method: Rdf#URI, noMeta: List[Agent]=List()): Script[Rdf, List[Agent]] = {
    getMeta(resource).flatMap{nr =>
      nr.acl.map { acl => getAuth(acl, method)}.getOrElse {
        `return`(noMeta)
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

