package org.w3.banana.sesame

import org.w3.banana.{SparqlAnswerXML, SparqlAnswerJson, RDFGraphQueryTest}

class SesameGraphQueryTest extends RDFGraphQueryTest[Sesame, SesameSPARQL, SparqlAnswerXML](
    SesameOperations,
    SesameDiesel,
    SesameRDFXMLReader,
    SesameGraphIsomorphism,
    SesameSPARQLOperations,
    SesameGraphQuery,
    SparqlAnswerWriter.XML,
    SparqlAnswerReader.XML)

