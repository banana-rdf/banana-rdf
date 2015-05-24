package org.w3.banana.bigdata

import org.scalatest._
import org.w3.banana._
import org.w3.banana.binder.CommonBindersTest
import org.w3.banana.diesel.{DieselGraphConstructTest, DieselGraphExplorationTest}


class BigdataOpsTest/*() extends RDFOpsTest[Bigdata]*/ (implicit ops: RDFOps[Bigdata]) extends WordSpec with Matchers with OptionValues {

  type Rdf = Bigdata

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

class BigdataCommonBindersTest extends CommonBindersTest[Bigdata]

class BigdataDieselGraphConstructTest extends DieselGraphConstructTest[Bigdata]

class BigdataDieselGraphExplorationTest extends DieselGraphExplorationTest[Bigdata]

class BigdataGraphTest extends GraphTest[Bigdata]

//class BigdataIsomorphismTest() extends IsomorphismTests[Bigdata]

//class BigdataPointedGraphTest extends PointedGraphTester[Bigdata]

//class BigdataRecordBinderTest extends RecordBinderTest[Bigdata] //BIGDATA DOES NOT SUPPORT RELATIVE URIs

//class BigdataUriSyntaxTest extends UriSyntaxTest[Bigdata]

