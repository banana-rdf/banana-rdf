package org.w3.banana.ldp

import akka.actor.Props
import akka.util.Timeout
import java.nio.file.Files
import java.security.interfaces.RSAPublicKey
import java.security.KeyPairGenerator
import java.util.concurrent.TimeUnit
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.scalatest.matchers.MustMatchers
import org.w3.banana._
import concurrent.Future
import java.net.{URL=>jURL,URI=>jURI}
import org.w3.banana.plantain.Plantain
import org.w3.banana.ldp.LDPCommand._
import scala.Some
import java.util.Date


object WebACLTestSuite {
  import org.w3.banana.plantain.model.URI
  implicit val timeout = Timeout(10,TimeUnit.MINUTES)
  val dir = Files.createTempDirectory("plantain" )
  val baseUri = URI.fromString("http://example.com/foo/")
  implicit val authz: AuthZ[Plantain] =  new AuthZ[Plantain]()(Plantain.ops)
  val rww = new RWWeb[Plantain](baseUri)(Plantain.ops,timeout)
  rww.setLDPSActor(rww.system.actorOf(Props(new PlantainLDPCActor(rww.baseUri, dir)),"rootContainer"))
}

class PlantainWebACLTest extends WebACLTestSuite[Plantain](
  WebACLTestSuite.rww,WebACLTestSuite.baseUri)(
  Plantain.ops,Plantain.sparqlOps,Plantain.sparqlGraph,Plantain.recordBinder,Plantain.rdfxmlReader,PlantainTest.authz)

abstract class WebACLTestSuite[Rdf<:RDF](rww: RWW[Rdf], baseUri: Rdf#URI)(
                                 implicit ops: RDFOps[Rdf],
                                 sparqlOps: SparqlOps[Rdf],
                                 sparqlGraph: SparqlGraph[Rdf],
                                 recordBinder: binder.RecordBinder[Rdf],
                                 reader: RDFReader[Rdf, RDFXML],
                                 authz: AuthZ[Rdf])
    extends WordSpec with MustMatchers with BeforeAndAfterAll with TestHelper {

  import ops._
  import syntax._
  import diesel._

  val certbinder = new CertBinder()
  import certbinder._

  implicit def toUri(url: jURL): Rdf#URI = URI(url.toString)

  val wac = WebACLPrefix[Rdf]
  val foaf = FOAFPrefix[Rdf]
  val cert = CertPrefix[Rdf]
  val keyGen = KeyPairGenerator.getInstance("RSA");
  val henryRsaKey: RSAPublicKey = { keyGen.initialize(768);  keyGen.genKeyPair().getPublic().asInstanceOf[RSAPublicKey] }
  val bertailsRsaKey: RSAPublicKey = { keyGen.initialize(512);  keyGen.genKeyPair().getPublic().asInstanceOf[RSAPublicKey] }


  val henryCard = URI("http://bblfish.net/people/henry/card")
  val henry =  URI(henryCard.toString+"#me")
  val henryGraph : Rdf#Graph = (
    URI("#me") -- cert.key ->- henryRsaKey
               -- foaf.name ->- "Henry"
    ).graph

  val henryCardAcl = URI("http://bblfish.net/people/henry/card;wac")
  val henryCardAclGraph: Rdf#Graph = (
     bnode("t1")
      -- wac.accessTo ->- henryCard
      -- wac.agent ->- henry
      -- wac.mode ->- wac.Read
      -- wac.mode ->- wac.Write
    ).graph union (
     bnode("t2")
       -- wac.accessTo ->- henryCard
       -- wac.agentClass ->- foaf.Agent
       -- wac.mode ->- wac.Read
    ).graph

  val groupACLForRegexResource: Rdf#Graph = (
    bnode("t1")
      -- wac.accessToClass ->- ( bnode("t2") -- wac.regex ->- "http://bblfish.net/blog/.*" )
      -- wac.agentClass ->- ( URI("http://bblfish.net/blog/editing/.meta#a1") -- foaf("member") ->- henry )
      -- wac.mode ->- wac.Read
      -- wac.mode ->- wac.Write
    ).graph

  case class TestLDPR(uri: Rdf#URI, graph: Rdf#Graph, metaGraph: Rdf#Graph=Graph.empty)(implicit val ops: RDFOps[Rdf]) extends LDPR[Rdf] {

    def updated = Some(new Date())

    /**
     * location of initial ACL for this resource
     **/
    def acl = Some{
      if (uri.toString.endsWith(";wac")) uri
      else ops.URI(uri.toString+";wac")
    }

    //move all the metadata to this, and have the other functions
    def meta = PointedGraph(uri,metaGraph)
  }

  object testFetcher extends ResourceFetcher[Rdf] {
    def fetch(url: jURL): Future[NamedResource[Rdf]] = {
      System.out.println(s"fetching($url)")
      URI(url.toString) match {
        case `henryCard` =>  Future.successful(TestLDPR(henryCard,henryGraph.resolveAgainst(henryCard)))
        case `henryCardAcl` =>  Future.successful(TestLDPR(henryCardAcl,henryCardAclGraph.resolveAgainst(henryCardAcl)))
      }
    }
  }
  rww.setWebActor( rww.system.actorOf(Props(new LDPWebActor[Rdf](baseUri,testFetcher)),"webActor")  )

  val webidVerifier = new WebIDVerifier(rww)

  val bertailsCollection = URI("http://example.com/foo/bertails/")
  val bertails = URI("http://example.com/foo/bertails/card#me")
  val bertailsCard = URI("http://example.com/foo/bertails/card")

  override def afterAll(): Unit = {
    rww.shutdown()
  }

  val bertailsCardgraph: Rdf#Graph = (
    URI("#me")
      -- foaf.name ->- "Alexandre".lang("fr")
      -- foaf.title ->- "Mr"
      -- cert.key ->- bertailsRsaKey
    ).graph


  "access to Henry's resource" when {

    "Henry can Authenticate" in {
      val futurePrincipal = webidVerifier.verifyWebID(henry.toString,henryRsaKey)
      val res = futurePrincipal.map{p=>
        assert(p.isInstanceOf[WebIDPrincipal] && p.getName == henry.toString)
      }
      res.getOrFail()
    }

    "Henry can access his profile" in {
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
           System.out.println(s"write=$write")
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
  }

  "Alex" when {

    "add bertails card" in {
      val script = rww.execute(for {
        rUri <- createContainer(baseUri,Some("bertails"),Graph.empty)
        card <- createLDPR(rUri,Some(bertailsCard.lastPathSegment),bertailsCardgraph)
        rGraph <- getLDPR(card)
      } yield {
        rUri must be(bertailsCollection)
        assert(rGraph.relativize(bertailsCard) isIsomorphicWith bertailsCardgraph)
      })
      script.getOrFail()

    }

    "can Authenticate" in {
      val futurePrincipal = webidVerifier.verifyWebID(bertails.toString,bertailsRsaKey)
      val res = futurePrincipal.map{p=>
        System.out.println(s"p=$p")
        assert(p.isInstanceOf[WebIDPrincipal] && p.getName == bertails.toString)
      }
      res.getOrFail()
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
