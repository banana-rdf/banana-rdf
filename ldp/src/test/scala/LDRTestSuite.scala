package org.w3.banana.ldp

import akka.actor.Props
import akka.util.Timeout
import java.net.{URL => jURL, URI => jURI}
import java.nio.file.{Path, Files}
import java.util.concurrent.TimeUnit
import org.scalatest.matchers.MustMatchers
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.w3.banana._
import org.w3.banana.ldp.LDPCommand._
import org.w3.banana.plantain.Plantain
import play.api.libs.iteratee._
import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration.Duration
import auth._


object LDRTestSuite {

  import org.w3.banana.plantain.model.URI

  val dir: Path = Files.createTempDirectory("plantain" )
  val baseUri = URI.fromString("http://example.com/foo/")
  val rootLDPCActorProps = Props(new PlantainLDPCActor(baseUri, dir))
}

class PlantainLDRTest extends LDRTestSuite[Plantain](LDRTestSuite.baseUri, LDRTestSuite.dir, LDRTestSuite.rootLDPCActorProps)(
  Plantain.ops,Plantain.sparqlOps,Plantain.sparqlGraph,
  Plantain.recordBinder,Plantain.turtleWriter,
  Plantain.rdfxmlReader)

/**
 * test the LinkedResource ~ and ~> implementations
 */
abstract class LDRTestSuite[Rdf<:RDF](baseUri: Rdf#URI, dir: Path, rootLDPCActorProps: Props )(
  implicit val ops: RDFOps[Rdf],
  sparqlOps: SparqlOps[Rdf],
  sparqlGraph: SparqlGraph[Rdf],
  val recordBinder: binder.RecordBinder[Rdf],
  turtleWriter: RDFWriter[Rdf,Turtle],
  reader: RDFReader[Rdf, RDFXML])
  extends WordSpec with MustMatchers with BeforeAndAfterAll with TestHelper with TestGraphs[Rdf] {

  import diesel._
  import ops._
  import syntax._

  implicit val timeout = Timeout(10,TimeUnit.MINUTES)
  val rww = new RWWeb[Rdf](baseUri)
  implicit val authz =  new WACAuthZ[Rdf](new WebResource(rww))
  rww.setLDPSActor(rww.system.actorOf(rootLDPCActorProps,"rootContainer"))
  rww.setWebActor( rww.system.actorOf(Props(new LDPWebActor[Rdf](baseUri,testFetcher)),"webActor")  )

  val webidVerifier = new WebIDVerifier(rww)

  val web = new WebResource[Rdf](rww)

  "setup Alex's profile" when {

    "add bertails card and acls" in {
      val script = rww.execute(for {
        ldpc     <- createContainer(baseUri,Some("bertails"),Graph.empty)
        ldpcMeta <- getMeta(ldpc)
        card     <- createLDPR(ldpc,Some(bertailsCard.lastPathSegment),bertailsCardGraph)
        cardMeta <- getMeta(card)
        _        <- updateLDPR(ldpcMeta.acl.get, add=bertailsContainerAclGraph.toIterable)
        _        <- updateLDPR(cardMeta.acl.get, add=bertailsCardAclGraph.toIterable)
        rGraph   <- getLDPR(card)
        aclGraph <- getLDPR(cardMeta.acl.get)
        containerAclGraph <- getLDPR(ldpcMeta.acl.get)
      } yield {
        ldpc must be(bertailsContainer)
        cardMeta.acl.get must be(bertailsCardAcl)
        assert(rGraph isIsomorphicWith (bertailsCardGraph union containsRel).resolveAgainst(bertailsCard))
        assert(aclGraph isIsomorphicWith bertailsCardAclGraph.resolveAgainst(bertailsCardAcl))
        assert(containerAclGraph isIsomorphicWith bertailsContainerAclGraph.resolveAgainst(bertailsContainerAcl))
      })
      script.getOrFail()

    }
  }

  "Test WebResource ~" in {
    val ldrEnum = web~(tpacGroup)
    val futureResList = ldrEnum(Iteratee.getChunks[LinkedDataResource[Rdf]]).flatMap(_.run)
    val resList = futureResList.getOrFail()
    resList must have length(1)
    val ldr = resList.head
    ldr.location must be(tpacGroupDoc)
    ldr.resource.pointer must be(tpacGroup)
    assert(ldr.resource.graph isIsomorphicWith tpacGroupGraph.resolveAgainst(tpacGroupDoc))
  }

  "Test WebResource ~>" in {
    val memberEnum: Enumerator[LinkedDataResource[Rdf]] = for {
      groupLdr <- web~(tpacGroup)
      member <-  web.~>(groupLdr,foaf.member)()
    } yield {
      member
    }
    val memberFuture = memberEnum(Iteratee.getChunks[LinkedDataResource[Rdf]]).flatMap(_.run)
    val memberList = memberFuture.getOrFail()

    val answersMap = Map(memberList.map{ldr => (ldr.resource.pointer,ldr.resource)} : _*)

    val pointers = answersMap.keys.toList
    answersMap must have size (3)

    assert(pointers.contains(henry))
    assert(pointers.contains(bertails))
    assert(pointers.contains(timbl))

    assert(answersMap(bertails).graph isIsomorphicWith((bertailsCardGraph union containsRel).resolveAgainst(bertailsCard)))
    assert(answersMap(henry).graph isIsomorphicWith(henryGraph.resolveAgainst(henryCard)))
    assert(answersMap(timbl).graph isIsomorphicWith(timblGraph.resolveAgainst(timblCard)))

  }

  "Test WebResource ~> followed by ~> to literal" in {
    val nameEnum = for {
      groupLdr <- web~(tpacGroup)
      member <-  web.~>(groupLdr,foaf.member)()
      name <- web.~>(member,foaf.name)()
    } yield {
      name
    }
    val nameFuture = nameEnum(Iteratee.getChunks[LinkedDataResource[Rdf]]).flatMap(_.run)
    val namesLDR = nameFuture.getOrFail()

    val answers = namesLDR.map{ldr => ldr.resource.pointer}
    answers must have length (3)
    assert(answers.contains(TypedLiteral("Henry")))
    assert(answers.contains("Alexandre".lang("fr")))
    assert(answers.contains(TypedLiteral("Tim Berners-Lee")))
  }

  "Test ACLs with ~ and ~> and <~ (tests bnode support too)" in {
    val nameEnum = for {
      wacLdr <- web~(henryFoafWac)
      auth  <-  web.<~(LinkedDataResource(wacLdr.location,PointedGraph(henryFoaf,wacLdr.resource.graph)),wac.accessTo)()
      agentClass <-  web.~>(auth,wac.agentClass)()
      member <-  web.~>(agentClass,foaf.member)()
      name <- web.~>(member,foaf.name)()
    } yield {
      name
    }
    val nameFuture = nameEnum(Iteratee.getChunks[LinkedDataResource[Rdf]]).flatMap(_.run)
    val namesLDR = nameFuture.getOrFail()

    val answers = namesLDR.map{ldr => ldr.resource.pointer}
    answers must have length (3)
    assert(answers.contains(TypedLiteral("Henry")))
    assert(answers.contains("Alexandre".lang("fr")))
    assert(answers.contains(TypedLiteral("Tim Berners-Lee")))
  }

  "Test WebResource ~> with missing remote resource" in {
    //we delete timbl
    val ex = rww.execute(deleteResource(timblCard))
    ex.getOrFail()
    val ex2 = rww.execute(getResource(timblCard))
    val res = ex2.failed.map{_=>true}
    assert{ Await.result(res,Duration(2,TimeUnit.SECONDS)) == true}
    //now we should only have two resources returned
    val memberEnum: Enumerator[LinkedDataResource[Rdf]] = for {
      groupLdr <- web~(tpacGroup)
      member <-  web.~>(groupLdr,foaf.member)()
    } yield {
      member
    }
    val memberFuture = memberEnum(Iteratee.getChunks[LinkedDataResource[Rdf]]).flatMap(_.run)
    val memberList = memberFuture.getOrFail()

    val answersMap = Map(memberList.map{ldr => (ldr.resource.pointer,ldr.resource)} : _*)

    val pointers = answersMap.keys.toList
    answersMap must have size (2)

    assert(pointers.contains(henry))
    assert(pointers.contains(bertails))

    assert(answersMap(bertails).graph isIsomorphicWith((bertailsCardGraph union containsRel).resolveAgainst(bertailsCard)))
    assert(answersMap(henry).graph isIsomorphicWith(henryGraph.resolveAgainst(henryCard)))

  }



}
