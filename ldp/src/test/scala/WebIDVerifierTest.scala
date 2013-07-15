package org.w3.banana.ldp

import akka.actor.Props
import akka.util.Timeout
import org.w3.banana.ldp.auth.{Claim, WebIDVerifier, WACAuthZ, X509CertSigner}
import java.net.URL
import java.nio.file.{Path, Files}
import java.util.concurrent.TimeUnit
import org.scalatest.matchers.MustMatchers
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.w3.banana._
import scala.concurrent.{ExecutionContext, Future}
import org.w3.banana.ldp.LDPCommand._
import scala.Some
import sun.security.x509.X500Name
import java.security.Principal

import org.w3.banana.plantain.Plantain

object PlantainWebIDVerifierTest {
  import org.w3.banana.plantain.model.URI
  val dir = Files.createTempDirectory("plantain" )
  val baseUri = URI.fromString("http://example.com/foo/")
  val rootLDPCActorProps = Props(new PlantainLDPCActor(baseUri, dir))
}

class PlantainWebIDVerifierTest extends WebIDVerifierTest[Plantain](
  PlantainWebIDVerifierTest.baseUri, PlantainWebIDVerifierTest.dir,PlantainWebIDVerifierTest.rootLDPCActorProps)(
  Plantain.ops,Plantain.sparqlOps,Plantain.recordBinder,Plantain.sparqlGraph, Plantain.turtleWriter,Plantain.rdfxmlReader)


/**
 * Test WebIDVerifier
 *
 */
abstract class WebIDVerifierTest[Rdf<:RDF](baseUri: Rdf#URI, dir: Path, rootLDPCActorProps: Props )
                                          (implicit val ops: RDFOps[Rdf],
                                           val sparqlOps: SparqlOps[Rdf],
                                           val recordBinder: binder.RecordBinder[Rdf],
                                           sparqlGrpah: SparqlGraph[Rdf],
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


  val bertailsCertSigner = new X509CertSigner(bertailsKeys.priv)
  val bertailsCert = bertailsCertSigner.generate(new X500Name("CN=Alex,O=W3C"),
    bertailsKeys.pub,2,new URL(bertails.toString))
  //of course bertails cannot create a cert with henry's public key, because not having the private key he would not
  //be able to connect to a server with it. So he must use a different key - his own will do for the test
  val bertailsFakeHenryCert = bertailsCertSigner.generate(new X500Name("CN=Henry, O=bblfish.net"),
    bertailsKeys.pub,3,new URL(henry.toString))

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

  "verify bertails' cert" in {
    val webidListFuture = webidVerifier.verify(Claim.ClaimMonad.point(bertailsCert))
    val futureWebids = Future.find(webidListFuture)(_=>true)
    val webidOpt = futureWebids.getOrFail()
    webidOpt.map{p=>p.getName} must be(Some(bertails.toString))
  }

  "verify bertails' fake cert fails" in {
    val webidListFuture = webidVerifier.verify(Claim.ClaimMonad.point(bertailsFakeHenryCert))
    val firstFuture: Future[Option[Principal]] = Future.find(webidListFuture)(_=>true)
    val res = firstFuture.getOrFail()
    res must be(None)
  }


  val henryCertSigner = new X509CertSigner(henryKeys.priv)
  val henryCert = henryCertSigner.generate(new X500Name("CN=Henry, O=bblfish.net"),henryKeys.pub,2,new URL(henry.toString))
  val henrysFakeBertailsCert = henryCertSigner.generate(new X500Name("CN=Alex,O=W3C"),henryKeys.pub,3,new URL(bertails.toString))


  "verify Henry's cert" in {
    val webidListFuture = webidVerifier.verify(Claim.ClaimMonad.point(henryCert))
    val futureWebids = Future.find(webidListFuture)(_=>true)
    val webids = futureWebids.getOrFail()
    webids.map{p=>p.getName} must be(Some(henry.toString))
  }

  "verify Henry's fake cert fails" in {
    val webidListFuture = webidVerifier.verify(Claim.ClaimMonad.point(henrysFakeBertailsCert))
    val futureWebids = Future.find(webidListFuture)(_=>true)
    val res = futureWebids.getOrFail()
    res must be(None)
  }


}
