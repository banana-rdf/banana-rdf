//import org.scalatest.{BeforeAndAfterAll, WordSpec}
//import org.scalatest.matchers.MustMatchers
//import org.w3.banana._
//import concurrent.{Await, Future}
//import concurrent.duration.Duration
//import java.net.URL
//import org.w3.banana.LinkedDataResource
//import org.w3.banana.plantain.{Subject, RWW}
//
//class WebACLTestSuite[Rdf<:RDF](implicit val ops: RDFOps[Rdf])
//    extends WordSpec with MustMatchers with BeforeAndAfterAll with TestHelper {
//
//  import ops._
//  val wac = WebACLPrefix[Rdf]
//  val foaf = FOAFPrefix[Rdf]
//  val rww: RWW[Rdf]
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
//
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
//  val henry = new java.net.URI("http://bblfish.net/people/henry/card#me")
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
//
//
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
//  val groupACLForRegexResource: Rdf#Graph = (
//    bnode("t1")
//      -- wac.accessToClass ->- ( bnode("t2") -- wac.regex ->- "http://bblfish.net/blog/editing/.*" )
//      -- wac.agentClass ->- ( URI("http://bblfish.net/blog/editing/.meta#a1") -- foaf("member") ->- URI(henry.toString) )
//      -- wac.mode ->- wac.Read
//      -- wac.mode ->- wac.Write
//    ).graph
//
//  val wac3 = WebAccessControl[Rdf](dummyCache(URI("http://bblfish.net/blog/editing/.meta"),groupACLForRegexResource))
//
//
//  "Access to group (named with a local uri) protected resources described by a regex" when {
////    wac3.authorizations must have size(1)
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
//
//
//}
