package org.w3.banana

import org.scalatest.{Matchers, WordSpec}


class RDFOpsTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends WordSpec with Matchers {

  import ops._

  "(de)constructing plain literal" in {

    val plainLiteral: Rdf#Literal = Literal("Alexandre")

    val (lexicalForm, datatype, langOpt) = fromLiteral(plainLiteral)

    lexicalForm shouldEqual "Alexandre"

    datatype shouldEqual xsd.string

    langOpt shouldEqual None

  }

  "(de)constructing typed literal" in {

    val foaf = FOAFPrefix[Rdf]

    val typedLiteral: Rdf#Literal = Literal("Alexandre", foaf.name)

    val (lexicalForm, datatype, langOpt) = fromLiteral(typedLiteral)

    lexicalForm shouldEqual "Alexandre"

    datatype shouldEqual foaf.name

    langOpt shouldEqual None

  }

  "(de)constructing lang literal" in {

    val langLiteral: Rdf#Literal = Literal.tagged("Alexandre", Lang("fr"))

    val (lexicalForm, datatype, langOpt) = fromLiteral(langLiteral)

    lexicalForm shouldEqual "Alexandre"

    datatype shouldEqual Literal.rdfLangString

    langOpt shouldEqual Some(Lang("fr"))

  }

  "Rdf#Node extractors" in {

    val initialNodes: List[Rdf#Node] = List(URI("http://example.com"), BNode(), Literal("foobar"))

    val nodes = initialNodes.map {
      case uri@URI(_)           => uri
      case bnode@BNode(_)       => bnode
      case lit@Literal(_, _, _) => lit
      case _                    => sys.error("should not arrive here")
    }

    initialNodes shouldEqual nodes

  }





}
