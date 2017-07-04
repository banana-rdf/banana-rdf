package org.w3.banana

import org.scalatest.WordSpec


class RDFOpsTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends WordSpec {

  import ops._

  "(de)constructing plain literal" in {

    val plainLiteral: Rdf#Literal = Literal("Alexandre")

    val (lexicalForm, datatype, langOpt) = fromLiteral(plainLiteral)

    assert(lexicalForm === ("Alexandre"))

    assert(datatype === (xsd.string))

    assert(langOpt === (None))

  }

  "(de)constructing typed literal" in {

    val foaf = FOAFPrefix[Rdf]

    val typedLiteral: Rdf#Literal = Literal("Alexandre", foaf.name)

    val (lexicalForm, datatype, langOpt) = fromLiteral(typedLiteral)

    assert(lexicalForm === ("Alexandre"))

    assert(datatype === (foaf.name))

    assert(langOpt === (None))

  }

  "(de)constructing lang literal" in {

    val langLiteral: Rdf#Literal = Literal.tagged("Alexandre", Lang("fr"))

    val (lexicalForm, datatype, langOpt) = fromLiteral(langLiteral)

    assert(lexicalForm === ("Alexandre"))

    assert(datatype === (Literal.rdfLangString))

    assert(langOpt === (Some(Lang("fr"))))

  }

  "Rdf#Node extractors" in {

    val initialNodes: List[Rdf#Node] = List(URI("http://example.com"), BNode(), Literal("foobar"))

    val nodes = initialNodes.map {
      case uri@URI(_)           => uri
      case bnode@BNode(_)       => bnode
      case lit@Literal(_, _, _) => lit
      case _                    => sys.error("should not arrive here")
    }

    assert(initialNodes === (nodes))

  }





}
