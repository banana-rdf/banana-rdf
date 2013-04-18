package org.w3.banana.ldp

import akka.actor.Props
import akka.util.Timeout
import java.net.{URL => jURL, URI => jURI}
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import org.scalatest.matchers.MustMatchers
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.w3.banana._
import org.w3.banana.ldp.LDPCommand._
import org.w3.banana.plantain.Plantain
import play.api.libs.iteratee._
import scala.Some
import scala.concurrent.Await
import scala.concurrent.duration.Duration


object WebTestSuite {

  import org.w3.banana.plantain.model.URI

  implicit val timeout = Timeout(10,TimeUnit.MINUTES)
  val dir = Files.createTempDirectory("plantain" )
  val baseUri = URI.fromString("http://example.com/foo/")
  implicit val authz: AuthZ[Plantain] =  new AuthZ[Plantain]()(Plantain.ops)
  val rww = new RWWeb[Plantain](baseUri)(Plantain.ops,timeout)
  rww.setLDPSActor(rww.system.actorOf(Props(new PlantainLDPCActor(rww.baseUri, dir)),"rootContainer"))
}

class PlantainWebTest extends WebTestSuite[Plantain](
  WebTestSuite.rww,WebTestSuite.baseUri)(
  Plantain.ops,Plantain.sparqlOps,Plantain.sparqlGraph,Plantain.recordBinder,Plantain.turtleWriter,Plantain.rdfxmlReader,PlantainTest.authz)

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
                                 reader: RDFReader[Rdf, RDFXML],
                                 authz: AuthZ[Rdf])
    extends WordSpec with MustMatchers with BeforeAndAfterAll with TestHelper with TestGraphs[Rdf] {

  import diesel._
  import ops._
  import syntax._


  rww.setWebActor( rww.system.actorOf(Props(new LDPWebActor[Rdf](baseUri,testFetcher)),"webActor")  )

  val webidVerifier = new WebIDVerifier(rww)


  "access to Henry's resources" when {

    "Henry can Authenticate" in {
      val futurePrincipal = webidVerifier.verifyWebID(henry.toString,henryRsaKey)
      val res = futurePrincipal.map{p=>
        assert(p.isInstanceOf[WebIDPrincipal] && p.getName == henry.toString)
      }
      res.getOrFail()
    }

    "Who can access Henry's WebID profile?" in {
      val ex = rww.execute{
         for {
          read <- authz.getAuthFor(henryCard,wac.Read)
          write <- authz.getAuthFor(henryCard,wac.Write)
         } yield {
            assert(read.exists{agent =>
              agent.contains(henry)
            })
           assert(read.exists{agent =>
             agent.contains(foaf.Agent)
           })
           assert(write.exists{agent =>
             agent.contains(henry)
           })
           assert(!write.exists{agent =>
             agent.contains(foaf.Agent)
           })
         }
       }
      ex.getOrFail()
    }

    "henry creates his foaf list ( no ACL here )" in {
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


    //    "Who can access Henry's foaf profile?" in {
//      val ex = rww.execute{
//        for {
//          read <- authz.getAuthFor(henryFoaf,wac.Read)
//          write <- authz.getAuthFor(henryFoaf,wac.Write)
//        } yield {
//          assert(read.exists{agent =>
//            agent.contains(timbl)
//          },"timbl is in group")
//          assert(read.exists{agent =>
//            agent.contains(bertails)
//          },"alex is in group")
//          assert(!read.exists{agent =>
//            agent.contains(foaf.Agent)
//          },"not everyone can see profile")
//          assert(write.exists{agent =>
//            agent.contains(henry)
//          })
//          assert(!write.exists{agent =>
//            agent.contains(foaf.Agent)
//          })
//        }
//      }
//      ex.getOrFail()
//    }
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
      val ex = rww.execute{
        for {
          read <- authz.getAuthFor(bertailsCard,wac.Read)
          write <- authz.getAuthFor(bertailsCard,wac.Write)
        } yield {
          assert(read.exists{agent =>
            agent.contains(bertails)
          })
          assert(read.exists{agent =>
            agent.contains(foaf.Agent)
          })
          assert(write.exists{agent =>
            agent.contains(bertails)
          })
          assert(!write.exists{agent =>
            agent.contains(henry)
          })
          assert(!write.exists{agent =>
            agent.contains(foaf.Agent)
          })
        }
      }
      ex.getOrFail()
    }

    "can Access other resources in Alex's container" in {
      val ex = rww.execute{
        for {
          read <- authz.getAuthFor(bertailsCard,wac.Read)
          write <- authz.getAuthFor(bertailsCard,wac.Write)
        } yield {
          assert(read.exists{agent =>
            agent.contains(bertails)
          })
          assert(read.exists{agent =>
            agent.contains(foaf.Agent)
          })
          assert(write.exists{agent =>
            agent.contains(bertails)
          })
          assert(!write.exists{agent =>
            agent.contains(foaf.Agent)
          })
        }
      }
      ex.getOrFail()
    }

  }


  "w3c WebID group" when {

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

  }



  //  "Henry" when {
//    //    wac3.authorizations must have size(1)
//    val webreq = DummyWebRequest( henryFuture, Read,
//      new URL("http://bblfish.net/blog/editing/.meta"),
//      new URL("http://bblfish.net/blog/editing/post1")
//    )
//
//    "read mode" in {
//      val fb=wac3.allow(webreq)
//      Await.result(fb,Duration("1s")) mustBe henrysubjAnswer
//    }
//    "write mode" in {
//      val fb=wac3.allow(webreq.copy2(mode=Write))
//      Await.result(fb,Duration("1s")) mustBe henrysubjAnswer
//    }
//    "control mode" in {
//      val fb=wac3.allow(webreq.copy2(mode=Control))
//      try {
//        Await.result(fb,Duration("1s"))
//        fail("Should throw an exception")
//      } catch {
//        case e: Exception => System.out.println("ok:"+e)
//      }
//    }
//
//  }
//
//

  //  def dummyCache(loc: Rdf#URI, g: Rdf#Graph) = new LinkedDataCache[Rdf]{
//    def get(uri: Rdf#URI) =
//      if (uri==loc) Future(LinkedDataResource(uri, PointedGraph[Rdf](uri,g)))
//      else throw new Exception("method should never be called")
//  }
//
//  def partialWebCache(loc: Rdf#URI, g: Rdf#Graph) = new LinkedDataCache[Rdf]{
//    def get(uri: Rdf#URI) =
//      if (uri==loc) Future(LinkedDataResource(uri, PointedGraph[Rdf](uri,g)))
//      else cache.get(uri)
//  }

//  case class DummyWebRequest(subject: Future[Subject], mode: Mode, meta: URL, url: URL)
//    extends WebRequest[Any] {
//
//    def copy2(subject: Future[Subject] = this.subject, mode: Mode = this.mode,
//               meta: URL=this.meta, url: URL=this.url) = {
//      val (_subj, _mode, _meta, _url) = (subject,mode,meta,url)
//      DummyWebRequest(_subj,_mode,_meta,_url)
//    }
//
//    def id = ???
//    def uri = ???
//    def method = ???
//    def tags = ???
//    def path = ???
//    def version = ???
//    def queryString = ???
//    def headers = ???
//    def certs(required: Boolean) = ???
//    def remoteAddress = ???
//    def body = ???
//  }
//
//  val henrysubj = Subject(List(WebIDPrincipal(henry)))
//  val henrysubjAnswer = Subject(List(WebIDPrincipal(henry)),List(WebIDPrincipal(henry)))
//  val henryFuture = Future(henrysubj)
//
//
//  val publicACLForSingleResource: Rdf#Graph = (
//    bnode("t1") -- wac.accessTo ->- URI("http://joe.example/pix/img")
//       -- wac.agentClass ->- foaf("Agent")
//       -- wac.mode ->- wac.Read
//    ).graph
//
//  val nonEvaluabableSubject: Future[Subject] = Future {
//     sys.error("the resource is public so the subject need not be evaluated")
//  }
//
//  val wac1 = WebAccessControl[Rdf](dummyCache(URI("http://joe.example/pix/.meta"),publicACLForSingleResource))


//  """Access to a Public resource by an individual
//    (see http://www.w3.org/wiki/WebAccessControl#Public_Access)""" when {
////    wac1.authorizations must have size(1)
//    val webreq = DummyWebRequest( nonEvaluabableSubject, Read,
//        new URL("http://joe.example/pix/.meta"),
//        new URL("http://joe.example/pix/img")
//    )
//    "read mode" in {
//      val fb=wac1.allow(webreq)
//      Await.result(fb,Duration("1s")) mustBe Anonymous
//    }
//    "write mode" in {
//      val fb=wac1.allow(webreq.copy2(mode=Write))
//      try {
//        Await.result(fb,Duration("1s"))
//        fail("Should throw an exception")
//      } catch {
//        case e: Exception => System.out.println("ok")
//      }
//    }
//    "control mode" in {
//      val fb=wac1.allow(webreq.copy2(mode=Control))
//      try {
//        Await.result(fb,Duration("1s"))
//        fail("Should throw an exception")
//      } catch {
//        case e: Exception => System.out.println("ok")
//      }
//    }
//  }
//
//  val publicACLForRegexResource: Rdf#Graph = (
//    bnode("t1")
//      -- wac.accessToClass ->- ( bnode("t2") -- wac.regex ->- "http://joe.example/blog/.*" )
//      -- wac.agentClass ->- foaf("Agent")
//      -- wac.mode ->- wac.Read
//    ).graph
//
//  val wac2 = WebAccessControl[Rdf](dummyCache(URI("http://joe.example/blog/.meta"),publicACLForRegexResource))
//
//
//  "Access to Public resources defined by a regex" when {
////    wac2.authorizations must have size(1)
//    val webreq = DummyWebRequest( nonEvaluabableSubject, Read,
//          new URL("http://joe.example/blog/.meta"),
//          new URL("http://joe.example/blog/2012/firstPost")
//    )
//
//    "read mode" in {
//      val fb=wac2.allow(webreq)
//      Await.result(fb,Duration("1s")) mustBe Anonymous
//
//    }
//    "write mode" in {
//      val fb = wac2.allow(webreq.copy2(mode=Write))
//      try {
//        Await.result(fb,Duration("1s"))
//        fail("Should throw an exception")
//      } catch {
//        case e: Exception => System.out.println("ok:"+e)
//      }
//    }
//    "control mode" in {
//      val fb = wac2.allow(webreq.copy2(mode=Control))
//      try {
//        Await.result(fb,Duration("1s"))
//        fail("Should throw an exception")
//      } catch {
//        case e: Exception => System.out.println("ok:"+e)
//      }
//    }
//  }
//

//
//  val remoteGroupACLForRegexResource: Rdf#Graph = (
//    bnode("t1")
//      -- wac.accessToClass ->- ( bnode("t2") -- wac.regex ->- "http://bblfish.net/blog/editing/.*" )
//      -- wac.agentClass ->- URI("http://www.w3.org/2005/Incubator/webid/team#we")
//      -- wac.mode ->- wac.Read
//      -- wac.mode ->- wac.Write
//    ).graph
//
//  val wac4 = WebAccessControl[Rdf](partialWebCache(URI("http://bblfish.net/blog/editing/.meta"),remoteGroupACLForRegexResource))
//
//
//  "Access to protected resources described by a regex to a group described remotely" when {
////    wac4.authorizations must have size(1)
//    val webreq = DummyWebRequest( henryFuture, Read,
//      new URL("http://bblfish.net/blog/editing/.meta"),
//      new URL("http://bblfish.net/blog/editing/post1")
//    )
//
//    "read mode" in {
//      val fb=wac4.allow(webreq)
//      Await.result(fb,Duration("15s")) mustBe henrysubjAnswer
//    }
//    "write mode" in {
//      val fb=wac4.allow(webreq.copy2(mode=Write))
//      Await.result(fb,Duration("15s")) mustBe henrysubjAnswer
//    }
//    "control mode" in {
//      val fb=wac4.allow(webreq.copy2(mode=Control))
//      try {
//        Await.result(fb,Duration("15s"))
//        fail("Should throw an exception")
//      } catch {
//        case e: Exception => System.out.println("ok:"+e)
//      }
//    }
//
//  }


}
