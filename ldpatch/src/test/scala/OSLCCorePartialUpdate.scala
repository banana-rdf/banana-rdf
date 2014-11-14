package org.w3.banana.ldpatch

import org.w3.banana._
import org.w3.banana.io._
import org.scalatest.{ Filter => _, _ }
import java.io._
import scala.util.{ Try, Success, Failure }
import org.w3.banana.ldpatch.model._

abstract class OSLCCorePartialUpdate[Rdf <: RDF](implicit
  ops: RDFOps[Rdf],
  reader: RDFReader[Rdf, Try, Turtle]
) extends WordSpec with Matchers { self =>

  import ops._

  val baseURI = "http://example.com/"

  def parseGraph(input: String): Rdf#Graph = reader.read(input, baseURI).get

  val g = new Grammar[Rdf] { implicit val ops = self.ops }

  def parseLDPatch(input: String): model.LDPatch[Rdf] = g.grammar.parseLDPatch(input, URI(baseURI)).get

  val s = new Semantics[Rdf] { implicit val ops = self.ops }

  def applyLDPatch(ldpatch: model.LDPatch[Rdf], graph: Rdf#Graph): Rdf#Graph = s.semantics.LDPatch(ldpatch, graph)

  // http://open-services.net/wiki/core/OSLC-Core-Partial-Update/#Example-update-blank-nodes-link-label
  "Example - update blank nodes (link label)" in {
    val graph = parseGraph("""
@prefix ex: <http://example.com/bugtracker> .
@prefix oslc: <http://open-services.net/ns/core#> .
@prefix oslc_cm: <http://open-services.net/ns/cm#> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix dcterms: <http://purl.org/dc/terms/> .

<http://example.com/bugs/2314>
   a oslc_cm:ChangeRequest ;
   dcterms:identifier " 00002314 " ;
   oslc:shortTitle "Bug 2314" ;
   dcterms:title "Invalid installation instructions" ;
   oslc:instanceShape <http://example.com/shapes/defect> ;
   dcterms:description "Invalid installation instructions indicating invalid patches to be applied." ;
   oslc:discussedBy <http://example.com/bugs/2314/discussion> ;
   oslc_cm:relatedChangeRequest <http://myserver/mycmapp/bugs/1235> ,
      <http://remoteserver/mycmapp/defects/abc123> ;
   ex:priority " Medium " ;
   ex:severity " Normal " .

_:b1 dcterms:title "A bad link title";
   rdf:object <http://myserver/mycmapp/bugs/1235>;
   rdf:predicate oslc_cm:relatedChangeRequest;
   rdf:subject <http://example.com/bugs/2314>;
   a rdf:Statement.
""")

    val expectedGraph = parseGraph("""
@prefix ex: <http://example.com/bugtracker> .
@prefix oslc: <http://open-services.net/ns/core#> .
@prefix oslc_cm: <http://open-services.net/ns/cm#> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix dcterms: <http://purl.org/dc/terms/> .

<http://example.com/bugs/2314>
   a oslc_cm:ChangeRequest ;
   dcterms:identifier " 00002314 " ;
   oslc:shortTitle "Bug 2314" ;
   dcterms:title "Invalid installation instructions" ;
   oslc:instanceShape <http://example.com/shapes/defect> ;
   dcterms:description "Invalid installation instructions indicating invalid patches to be applied." ;
   oslc:discussedBy <http://example.com/bugs/2314/discussion> ;
   oslc_cm:relatedChangeRequest <http://myserver/mycmapp/bugs/1235> ,
      <http://remoteserver/mycmapp/defects/abc123> ;
   ex:priority " Medium " ;
   ex:severity " Normal " .

_:b1 dcterms:title "A very good link title";
   rdf:object <http://myserver/mycmapp/bugs/1235>;
   rdf:predicate oslc_cm:relatedChangeRequest;
   rdf:subject <http://example.com/bugs/2314>;
   a rdf:Statement.
""")

    val ldpatch = parseLDPatch("""
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>.
@prefix dcterms: <http://purl.org/dc/terms/>.
@prefix oslc_cm: <http://open-services.net/ns/cm#> .

Bind ?label <http://example.com/bugs/2314>/^rdf:subject[/rdf:predicate = oslc_cm:relatedChangeRequest][/rdf:object = <http://myserver/mycmapp/bugs/1235>] .

Delete ?label dcterms:title "A bad link title" .
Add { ?label dcterms:title "A very good link title" } .
""")

    applyLDPatch(ldpatch, graph).isIsomorphicWith(expectedGraph) should be (true)

  }

}

import org.w3.banana.jena._

class OSLCCorePartialUpdateWithJena extends OSLCCorePartialUpdate[Jena]
