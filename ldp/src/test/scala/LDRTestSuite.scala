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
import scala.concurrent.Await
import scala.concurrent.duration.Duration


object LDRTestSuite {

  import org.w3.banana.plantain.model.URI

  implicit val timeout = Timeout(10,TimeUnit.MINUTES)
  val dir = Files.createTempDirectory("plantain" )
  val baseUri = URI.fromString("http://example.com/foo/")
  implicit val authz: AuthZ[Plantain] =  new AuthZ[Plantain]()(Plantain.ops)
  val rww = new RWWeb[Plantain](baseUri)(Plantain.ops,timeout)
  rww.setLDPSActor(rww.system.actorOf(Props(new PlantainLDPCActor(rww.baseUri, dir)),"rootContainer"))
}

class PlantainLDRTest extends LDRTestSuite[Plantain](
  LDRTestSuite.rww,LDRTestSuite.baseUri)(
  Plantain.ops,Plantain.sparqlOps,Plantain.sparqlGraph,
  Plantain.recordBinder,Plantain.turtleWriter,
  Plantain.rdfxmlReader,PlantainTest.authz)

/**
 * test the LinkedResource ~ and ~> implementations
 */
abstract class LDRTestSuite[Rdf<:RDF](rww: RWW[Rdf], baseUri: Rdf#URI)(
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

  val web = new WebResource[Rdf](rww)

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
      member <-  web~>(groupLdr,foaf.member)
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

    assert(answersMap(bertails).graph isIsomorphicWith(bertailsCardGraph.resolveAgainst(bertailsCard)))
    assert(answersMap(henry).graph isIsomorphicWith(henryGraph.resolveAgainst(henryCard)))
    assert(answersMap(timbl).graph isIsomorphicWith(timblGraph.resolveAgainst(timblCard)))

  }

  "Test WebResource ~> followed by ~> to literal" in {
    val nameEnum = for {
      groupLdr <- web~(tpacGroup)
      member <-  web~>(groupLdr,foaf.member)
      name <- web~>(member,foaf.name)
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
      auth  <-  web<~(LinkedDataResource(wacLdr.location,PointedGraph(henryFoaf,wacLdr.resource.graph)),wac.accessTo)
      agentClass <-  web~>(auth,wac.agentClass)
      member <-  web~>(agentClass,foaf.member)
      name <- web~>(member,foaf.name)
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
      member <-  web~>(groupLdr,foaf.member)
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
    //assert(pointers.contains(timbl))

    assert(answersMap(bertails).graph isIsomorphicWith(bertailsCardGraph.resolveAgainst(bertailsCard)))
    assert(answersMap(henry).graph isIsomorphicWith(henryGraph.resolveAgainst(henryCard)))
    //assert(answersMap(timbl).graph isIsomorphicWith(timblGraph.resolveAgainst(timblCard)))

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
