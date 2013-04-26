package org.w3.banana.ldp

import akka.actor.Props
import akka.util.Timeout
import java.net.{URL => jURL, URI => jURI}
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import org.scalatest.matchers.MustMatchers
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, WordSpec}
import org.w3.banana._
import org.w3.banana.ldp.LDPCommand._
import org.w3.banana.plantain.Plantain
import play.api.libs.iteratee._
import scala.Some
import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration.Duration


object WebTestSuite {

  import org.w3.banana.plantain.model.URI
  implicit val ec = ExecutionContext.Implicits.global

  implicit val timeout = Timeout(10,TimeUnit.MINUTES)
  val dir = Files.createTempDirectory("plantain" )
  val baseUri = URI.fromString("http://example.com/foo/")
  val rww = new RWWeb[Plantain](baseUri)(Plantain.ops,timeout)
  rww.setLDPSActor(rww.system.actorOf(Props(new PlantainLDPCActor(rww.baseUri, dir)),"rootContainer"))
}

class PlantainWebTest extends WebTestSuite[Plantain](
  WebTestSuite.rww,WebTestSuite.baseUri)(
  Plantain.ops,Plantain.sparqlOps,Plantain.sparqlGraph,
  Plantain.recordBinder,Plantain.turtleWriter,Plantain.rdfxmlReader)

/**
 *
 * tests the local and remote LDPR request, creation, LDPC creation, access control, etc...
 */
abstract class WebTestSuite[Rdf<:RDF](rww: RWW[Rdf], baseUri: Rdf#URI)(
                                 implicit val ops: RDFOps[Rdf],
                                  sparqlOps: SparqlOps[Rdf],
                                  sparqlGraph: SparqlGraph[Rdf],
                                 val recordBinder: binder.RecordBinder[Rdf],
                                 turtleWriter: RDFWriter[Rdf,Turtle],
                                 reader: RDFReader[Rdf, RDFXML])
    extends WordSpec with MustMatchers  with TestHelper with TestGraphs[Rdf]  {

    import diesel._
    import ops._
    import syntax._


  implicit val ec = ExecutionContext.Implicits.global

  rww.setWebActor( rww.system.actorOf(Props(new LDPWebActor[Rdf](baseUri,testFetcher)),"webActor")  )
  val webidVerifier = new WebIDVerifier(rww)
  implicit val authz: AuthZ[Rdf] =  new AuthZ[Rdf]()(ops,new WebResource(rww))



  "access to Henry's resources" when {

    "Henry's card to its acl" in {
      val ex = authz.acl(henryCard)
      val aclsFuture = ex.run(Iteratee.fold(List[LinkedDataResource[Rdf]]()){case (lst,ldr)=> ldr::lst })
      val res = aclsFuture.map{ res =>
        res.size must be(1)
        res(0).location must be(henryCardAcl)
        assert(res(0).resource.graph isIsomorphicWith henryCardAclGraph.resolveAgainst(henryCard))
      }
      res.getOrFail()
    }


    "Henry can Authenticate" in {
      val futurePrincipal = webidVerifier.verifyWebID(henry.toString,henryRsaKey)
      val res = futurePrincipal.map{p=>
        assert(p.isInstanceOf[WebIDPrincipal] && p.getName == henry.toString)
      }
      res.getOrFail()
    }

    "Who can access Henry's WebID profile?" in {
      val ex = for {
          read <- authz.getAuthFor(henryCard,wac.Read)
          write <- authz.getAuthFor(henryCard,wac.Write)
         } yield {
           assert(read.contains(henry))
           assert(read.contains(foaf.Agent))
           assert(write.contains(henry))
           assert(!write.contains(foaf.Agent))
         }
      ex.getOrFail()
    }

    "henry creates his foaf list ( no ACL here )" in {
      testFetcher.synMap -= henryFoaf
      val ex = rww.execute{
        for {
          foaf <- createLDPR(henryColl,Some("foaf"),henryFoafGraph)
          collGr <- getLDPR(henryColl)
        } yield {
          foaf must be(henryFoaf)
          assert{
            (PointedGraph(henryColl,collGr)/rdfs.member).exists(_.pointer == henryFoaf)
          }
        }
      }
      ex.getOrFail()
    }

    "Who can access Henry's foaf profile?" in {
      val ex =
        for {
          read <- authz.getAuthFor(henryFoaf,wac.Read)
         write <- authz.getAuthFor(henryFoaf,wac.Write)
        } yield {
          assert(read.contains(timbl),"timbl can read")
          assert(read.contains(bertails),"alex can read")
          assert(read.contains(henry),"henry can read")
          assert(!read.contains(foaf.Agent),"not everyone can read profile")
          assert(write.contains(henry))
          assert(!write.contains(foaf.Agent))
        }

      ex.getOrFail()
    }
  }



  "Alex's profile" when {

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
        assert(rGraph isIsomorphicWith bertailsCardGraph.resolveAgainst(bertailsCard))
        assert(aclGraph isIsomorphicWith bertailsCardAclGraph.resolveAgainst(bertailsCardAcl))
        assert(containerAclGraph isIsomorphicWith bertailsContainerAclGraph.resolveAgainst(bertailsContainerAcl))
      })
      script.getOrFail()

    }

    "Alex can Authenticate" in {
      val futurePrincipal = webidVerifier.verifyWebID(bertails.toString,bertailsRsaKey)
      val res = futurePrincipal.map{p=>
        assert(p.isInstanceOf[WebIDPrincipal] && p.getName == bertails.toString)
      }
      res.getOrFail()
    }

    "can Access Alex's profile" in {
      val ex = for {
          read <- authz.getAuthFor(bertailsCard,wac.Read)
          write <- authz.getAuthFor(bertailsCard,wac.Write)
        } yield {
          println(s"~~~> read=$read")
          println(s"~~~> write=$write")
          assert(read.contains(bertails))
          assert(read.contains(foaf.Agent))
          assert(write.contains(bertails))
          assert(!write.contains(henry))
          assert(!write.contains(foaf.Agent))
        }
      ex.getOrFail()
    }

    "can Access other resources in Alex's container" in {
      val ex = for {
        read <- authz.getAuthFor(bertailsCard, wac.Read)
        write <- authz.getAuthFor(bertailsCard, wac.Write)
      } yield {
        assert(read.contains(bertails))
        assert(read.contains(foaf.Agent))
        assert(write.contains(bertails))
        assert(!write.contains(foaf.Agent))
      }
      ex.getOrFail()
    }

  }


  "w3c WebID group" when {

    // we are going to create these
    testFetcher.synMap -= webidColl
    testFetcher.synMap -= tpacGroupDoc

    "tpac group creation" in {
      val ex = rww.execute {
        for {
          tpac <- createContainer(webidColl, Some("tpac"), Graph.empty)
          tpacGroup <- createLDPR(tpac, Some("group"), tpacGroupGraph)
          graph <- getLDPR(tpacGroup)
        } yield {
          tpac must be(tpacColl)
          tpacGroup must be(tpacGroupDoc)
          assert(graph.relativize(tpacGroupDoc) isIsomorphicWith (tpacGroupGraph))
        }
      }
      ex.getOrFail()
    }

    ""

  }



}
