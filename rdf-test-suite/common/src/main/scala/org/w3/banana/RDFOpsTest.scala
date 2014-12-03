package org.w3.banana

import zcheck.SpecLite

class RDFOpsTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends SpecLite {

  import ops._

  "(de)constructing plain literal" in {

    val plainLiteral: Rdf#Literal = Literal("Alexandre")

    val (lexicalForm, datatype, langOpt) = fromLiteral(plainLiteral)

    lexicalForm must_==("Alexandre")

    datatype must_==(xsd.string)

    langOpt must_==(None)

  }

  "(de)constructing typed literal" in {

    val foaf = FOAFPrefix[Rdf]

    val typedLiteral: Rdf#Literal = Literal("Alexandre", foaf.name)

    val (lexicalForm, datatype, langOpt) = fromLiteral(typedLiteral)

    lexicalForm must_==("Alexandre")

    datatype must_==(foaf.name)

    langOpt must_==(None)

  }

  "(de)constructing lang literal" in {

    val langLiteral: Rdf#Literal = Literal.tagged("Alexandre", Lang("fr"))

    val (lexicalForm, datatype, langOpt) = fromLiteral(langLiteral)

    lexicalForm must_==("Alexandre")

    datatype must_==(Literal.rdfLangString)

    langOpt must_==(Some(Lang("fr")))

  }

}
