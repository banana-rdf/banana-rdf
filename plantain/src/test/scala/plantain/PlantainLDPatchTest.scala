package org.w3.banana.plantain

import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import org.w3.banana.{RDFOps, SparqlOps, RDF}
import org.w3.banana.plantain.LDPatch
import org.w3.banana._
import scala.util.Try

class PlantainLDPatchTest extends LDPatchTest[Plantain](PlantainLDPatch)

abstract class LDPatchTest[Rdf<:RDF](ldpatch: LDPatch[Rdf,Try])
                               (implicit ops: RDFOps[Rdf],sparqlOps: SparqlOps[Rdf])
  extends WordSpec with MustMatchers with BeforeAndAfterAll {

  import sparqlOps._
  import ops._
  import syntax._
  import diesel._
  import scala.concurrent.ExecutionContext.Implicits.global

  val foaf = FOAFPrefix[Rdf]
  val wac = WebACLPrefix[Rdf]


  val g1 = ( URI("http://joe.name/#me") -- foaf.knows ->- URI("http://jane.name/#her") ).graph

  "SPARQL Insert Data on an empty graphs " in {
    val updtQuery = UpdateQuery(
      """
        | PREFIX foaf: <http://xmlns.com/foaf/0.1/>
        | INSERT DATA { <http://joe.name/#me> foaf:knows <http://jane.name/#her> }
      """.stripMargin)

    val resultGraph = ldpatch.executePatch(Graph.empty,updtQuery).get
    resultGraph.isIsomorphicWith(g1)
  }

  "SPARQL Delete Data removes only relation " in {
    val updtQuery = UpdateQuery(
      """
        | PREFIX foaf: <http://xmlns.com/foaf/0.1/>
        | DELETE DATA { <http://joe.name/#me> foaf:knows <http://jane.name/#her> }
      """.stripMargin)

    val resultGraph = ldpatch.executePatch(g1,updtQuery).get
    resultGraph.isIsomorphicWith(Graph.empty)
  }

  val timbl =    URI("http://www.w3.org/People/Berners-Lee/card#i")
  val bertails = URI("http://example.com/foo/bertails/card#me")
  val henryCard = URI("http://bblfish.net/people/henry/card")
  val henry =  URI(henryCard.toString+"#me")

  lazy val henryFoafGraph: Rdf#Graph = (
    henry.a(foaf.Person)
      -- foaf.knows ->- timbl
      -- foaf.knows ->- bertails
    ).graph

  "SPARQL DELETE with WHERE clause" in {
    val updtQuery = UpdateQuery(
      s"""
        | PREFIX foaf: <http://xmlns.com/foaf/0.1/>
        | DELETE { <$henry> foaf:knows ?p } WHERE { <$henry> foaf:knows ?p }
      """.stripMargin)

    val resultGraph = ldpatch.executePatch(g1,updtQuery).get
    val res =  ( henry.a(foaf.Person) ).graph
    resultGraph.isIsomorphicWith(res)

  }

  val bertailsContainer =    URI("http://example.com/foo/bertails/")
  val bertailsContainerAclGraph: Rdf#Graph = (
    bnode("t2")
      -- wac.accessToClass ->- ( bnode -- wac.regex ->- (bertailsContainer.toString+".*") )
      -- wac.agent ->- bertails
      -- wac.mode ->- wac.Write
      -- wac.mode ->- wac.Read
    ).graph
  val bertailsContainerAclGraph2: Rdf#Graph = (
    bnode("t2")
      -- wac.accessToClass ->- foaf.Agent
      -- wac.agent ->- bertails
      -- wac.mode ->- wac.Write
      -- wac.mode ->- wac.Read
    ).graph

  "SPARQL UPDATE with WHERE clause and bnodes" in {
    val updtQuery = UpdateQuery(
      s"""
        | PREFIX wac: <http://www.w3.org/ns/auth/acl#>
        | PREFIX foaf: <http://xmlns.com/foaf/0.1/>
        | DELETE { ?acl wac:accessToClass ?clzz .
        |          ?clzz wac:regex ?regex . }
        | INSERT { ?acl wac:accessToClass foaf:Agent }
        | WHERE { ?acl wac:accessToClass ?clzz .
                              ?clzz wac:regex ?regex . }
      """.stripMargin)
    val resultGraph = ldpatch.executePatch(bertailsContainerAclGraph,updtQuery).get
    resultGraph.isIsomorphicWith(bertailsContainerAclGraph2)

  }

}
