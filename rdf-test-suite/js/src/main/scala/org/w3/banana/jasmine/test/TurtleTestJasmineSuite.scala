package org.w3.banana.jasmine.test

import java.io.{ FileInputStream, File }

import org.w3.banana.{ RDFStore => RDFStoreInterface, _ }

import scala.concurrent.ExecutionContext
import scala.scalajs.test.JasmineTest

/**
 * Ported by Antonio Garrotte from rdf-test-suite in scala.tests to Jasmine Tests
 */
abstract class TurtleTestJasmineSuite[Rdf <: RDF]()(implicit ops: RDFOps[Rdf],
  reader: RDFReader[Rdf, Turtle],
  writer: RDFWriter[Rdf, Turtle])
    extends JasmineTest {

  import ops._

  def graphBuilder(prefix: Prefix[Rdf]) = {
    val ntriplesDoc = prefix("ntriples/")
    val creator = URI("http://purl.org/dc/elements/1.1/creator")
    val publisher = URI("http://purl.org/dc/elements/1.1/publisher")
    val dave = Literal("Dave Beckett")
    val art = Literal("Art Barstow")
    val w3org = URI("http://www.w3.org/")
    Graph(
      Triple(ntriplesDoc, creator, dave),
      Triple(ntriplesDoc, creator, art),
      Triple(ntriplesDoc, publisher, w3org))
  }

  val rdfCore = "http://www.w3.org/2001/sw/RDFCore/"
  val rdfCorePrefix = Prefix("rdf", rdfCore)
  val referenceGraph = graphBuilder(rdfCorePrefix)

  // TODO: there is a bug in Sesame with hash uris as prefix
  val foo = "http://example.com/foo/"
  val fooPrefix = Prefix("foo", foo)
  val fooGraph = graphBuilder(fooPrefix)

  val card_ttl =
    """
      |#Processed by Id: cwm.py,v 1.197 2007/12/13 15:38:39 syosi Exp
      |        #    using base file:///devel/WWW/People/Berners-Lee/card.n3
      |             @prefix : <http://xmlns.com/foaf/0.1/> .
      |    @prefix B: <http://www.w3.org/People/Berners-Lee/> .
      |    @prefix Be: <./> .
      |    @prefix blog: <http://dig.csail.mit.edu/breadcrumbs/blog/> .
      |    @prefix card: <http://www.w3.org/People/Berners-Lee/card#> .
      |    @prefix cc: <http://creativecommons.org/ns#> .
      |    @prefix cert: <http://www.w3.org/ns/auth/cert#> .
      |    @prefix con: <http://www.w3.org/2000/10/swap/pim/contact#> .
      |    @prefix dc: <http://purl.org/dc/elements/1.1/> .
      |    @prefix dct: <http://purl.org/dc/terms/> .
      |    @prefix doap: <http://usefulinc.com/ns/doap#> .
      |    @prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> .
      |    @prefix owl: <http://www.w3.org/2002/07/owl#> .
      |    @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
      |    @prefix s: <http://www.w3.org/2000/01/rdf-schema#> .
      |    @prefix w3c: <http://www.w3.org/data#> .
      |    @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
      |
      |    <../../DesignIssues/Overview.html>     dc:title "Design Issues for the World Wide Web";
      |         :maker card:i .
      |
      |    <>     rdf:type :PersonalProfileDocument;
      |         cc:license <http://creativecommons.org/licenses/by-nc/3.0/>;
      |         dc:title "Tim Berners-Lee's FOAF file";
      |         :maker card:i;
      |         :primaryTopic card:i .
      |
      |    <#i>     cert:key  [
      |             rdf:type cert:RSAPublicKey;
      |             cert:exponent 65537;
      |             cert:modulus "d7a0e91eedddcc905d5eccd1e412ab0c5bdbe118fa99b7132d915452f0b09af5ebc0096ca1dbdeec32723f5ddd2b05564e2ce67effba8e86778e114a02a3907c2e6c6b28cf16fee77d0ef0c44d2e3ccd3e0b6e8cfdd197e3aa86ec199980729af4451f7999bce55eb34bd5a5350470463700f7308e372bdb6e075e0bb8a8dba93686fa4ae51317a44382bb09d09294c1685b1097ffd59c446ae567faece6b6aa27897906b524a64989bd48cfeaec61d12cc0b63ddb885d2dadb0b358c666aa93f5a443fb91fc2a3dc699eb46159b05c5758c9f13ed2844094cc539e582e11de36c6733a67b5125ef407b329ef5e922ca5746a5ffc67b650b4ae36610fca0cd7b" ] .
      |
      |    <http://dig.csail.mit.edu/2005/ajar/ajaw/data#Tabulator>     doap:developer card:i .
      |
      |    <http://dig.csail.mit.edu/2007/01/camp/data#course>     :maker card:i .
      |
      |    <http://dig.csail.mit.edu/2008/webdav/timbl/foaf.rdf>     rdf:type :PersonalProfileDocument;
      |         cc:license <http://creativecommons.org/licenses/by-nc/3.0/>;
      |         dc:title "Tim Berners-Lee's editable FOAF file";
      |         :maker card:i;
      |         :primaryTopic card:i .
      |
      |# there is a bug in sesame when parsing blog:4
      |#    blog:4     dc:title "timbl's blog";
      |    <http://dig.csail.mit.edu/breadcrumbs/blog/4>     dc:title "timbl's blog";
      |         s:seeAlso <http://dig.csail.mit.edu/breadcrumbs/blog/feed/4>;
      |         :maker card:i .
      |
      |    <http://dig.csail.mit.edu/data#DIG>     :member card:i .
      |
      |    <http://wiki.ontoworld.org/index.php/_IRW2006>     dc:title "Identity, Reference and the Web workshop 2006";
      |         con:participant card:i .
      |
      |    <http://www.ecs.soton.ac.uk/~dt2/dlstuff/www2006_data#panel-panelk01>     s:label "The Next Wave of the Web (Plenary Panel)";
      |         con:participant card:i .
      |
      |    <http://www.w3.org/2000/10/swap/data#Cwm>     doap:developer card:i .
      |
      |    <http://www.w3.org/2011/Talks/0331-hyderabad-tbl/data#talk>     dct:title "Designing the Web for an Open Society";
      |         :maker card:i .
      |
      |    card:i     rdf:type con:Male,
      |                :Person;
      |         s:label "Tim Berners-Lee";
      |         s:seeAlso <http://dig.csail.mit.edu/2008/webdav/timbl/foaf.rdf>,
      |                <http://www.w3.org/2007/11/Talks/search/query?date=All+past+and+future+talks&event=None&activity=None&name=Tim+Berners-Lee&country=None&language=None&office=None&rdfOnly=yes&submit=Submit>;
      |         con:assistant card:amy;
      |         con:homePage Be:;
      |         con:office  [
      |             con:address  [
      |                 con:city "Cambridge";
      |                 con:country "USA";
      |                 con:postalCode "02139";
      |                 con:street "32 Vassar Street";
      |                 con:street2 "MIT CSAIL Room 32-G524" ];
      |             con:phone <tel:+1-617-253-5702>;
      |             geo:location  [
      |                 geo:lat "42.361860";
      |                 geo:long "-71.091840" ] ];
      |         con:preferredURI "http://www.w3.org/People/Berners-Lee/card#i";
      |         con:publicHomePage Be:;
      |         owl:sameAs <http://graph.facebook.com/512908782#>,
      |                <http://identi.ca/user/45563>,
      |                <http://www.advogato.org/person/timbl/foaf.rdf#me>,
      |                <http://www4.wiwiss.fu-berlin.de/bookmashup/persons/Tim+Berners-Lee>,
      |                <http://www4.wiwiss.fu-berlin.de/dblp/resource/person/100007>;
      |         :account <http://en.wikipedia.org/wiki/User:Timbl>,
      |                <http://identi.ca/timbl>,
      |                <http://twitter.com/timberners_lee>;
      |         :based_near  [
      |             geo:lat "42.361860";
      |             geo:long "-71.091840" ];
      |         :family_name "Berners-Lee";
      |         :givenname "Timothy";
      |         :homepage B:;
      |         :img <http://www.w3.org/Press/Stock/Berners-Lee/2001-europaeum-eighth.jpg>;
      |         :mbox <mailto:timbl@w3.org>;
      |         :mbox_sha1sum "965c47c5a70db7407210cef6e4e6f5374a525c5c";
      |         :name "Timothy Berners-Lee";
      |         :nick "TimBL",
      |                "timbl";
      |         :openid B:;
      |         :phone <tel:+1-(617)-253-5702>;
      |         :title "Sir";
      |# same bug again
      |#         :weblog blog:4;
      |         :weblog <http://dig.csail.mit.edu/breadcrumbs/blog/4>;
      |         :workplaceHomepage <http://www.w3.org/> .
      |
      |    w3c:W3C     :member card:i .
      |
      |    <http://www4.wiwiss.fu-berlin.de/booksMeshup/books/006251587X>     dc:creator card:i;
      |         dc:title "Weaving the Web: The Original Design and Ultimate Destiny of the World Wide Web" .
    """.stripMargin

  def asyncTest[Rdf <: RDF](text: String, base: String)(implicit executor: ExecutionContext): Array[Rdf#Graph] = {
    val graphs: Array[Any] = new Array[Any](1)

    reader.read(text, base).map {
      case g => {
        graphs(0) = g
      }
    }

    graphs.asInstanceOf[Array[Rdf#Graph]]
  }

  import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  describe("TURTLE parser") {

    it("read TURTLE version of timbl's card") {
      val file = new File("rdf-test-suite/src/main/resources/card.ttl")
      val fis = new FileInputStream(file)
      val graph = reader.read(fis, file.toURI.toString).get
      expect(graph.size) toEqual 77
      /*
      jasmine.Clock.useMock()
      val g: Array[Rdf#Graph] = asyncTest[Rdf](card_ttl, "http://test.com/card.ttl")
      jasmine.Clock.tick(10)
      expect(g(0).toIterable.size == 77).toEqual(true)
      */
    }

    it("read simple TURTLE String") {
      jasmine.Clock.useMock()
      val turtleString = """
<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Dave Beckett", "Art Barstow" ;
                                              <http://purl.org/dc/elements/1.1/publisher> <http://www.w3.org/> .
                         """
      val g: Array[Rdf#Graph] = asyncTest[Rdf](turtleString, rdfCore)
      jasmine.Clock.tick(10)
      val graph = g(0)
      expect(referenceGraph isIsomorphicWith graph).toEqual(true)

    }

    it("write simple graph as TURTLE string") {
      val turtleString = writer.asString(referenceGraph, "http://www.w3.org/2001/sw/RDFCore/").get
      expect(turtleString.isEmpty).toEqual(false)

      jasmine.Clock.useMock()
      val g: Array[Rdf#Graph] = asyncTest[Rdf](turtleString, rdfCore)
      jasmine.Clock.tick(10)
      val graph = g(0)
      expect(referenceGraph isIsomorphicWith graph).toEqual(true)
    }

    it("works with relative uris") {
      val turtleString = writer.asString(referenceGraph, rdfCore).get
      println("turtleString=" + turtleString)
      jasmine.Clock.useMock()
      val g: Array[Rdf#Graph] = asyncTest[Rdf](turtleString, foo)
      jasmine.Clock.tick(10)
      val graph = g(0)

      //      println(fooGraph.asInstanceOf[RDFStoreGraph].graph.toNT())
      //      println("***")
      //      println(graph.asInstanceOf[RDFStoreGraph].graph.toNT())

      expect(fooGraph isIsomorphicWith graph).toEqual(true)
    }

  }
}
