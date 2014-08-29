package org.w3.banana

import org.scalatest._

abstract class RDFOpsTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf])
    extends WordSpec with Matchers with OptionValues {

  import ops._

  "(de)constructing plain literal" in {

    val plainLiteral: Rdf#Literal = Literal("Alexandre")

    val (lexicalForm, datatype, langOpt) = fromLiteral(plainLiteral)

    lexicalForm should be("Alexandre")

    datatype should be(xsd.string)

    langOpt should be(None)

  }

  "(de)constructing typed literal" in {

    val foaf = FOAFPrefix[Rdf]

    val typedLiteral: Rdf#Literal = Literal("Alexandre", foaf.name)

    val (lexicalForm, datatype, langOpt) = fromLiteral(typedLiteral)

    lexicalForm should be("Alexandre")

    datatype should be(foaf.name)

    langOpt should be(None)

  }

  "(de)constructing lang literal" in {

    val langLiteral: Rdf#Literal = Literal.tagged("Alexandre", Lang("fr"))

    val (lexicalForm, datatype, langOpt) = fromLiteral(langLiteral)

    lexicalForm should be("Alexandre")

    datatype should be(Literal.rdfLangString)

    langOpt.value should be("fr")

  }

}
