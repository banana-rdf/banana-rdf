package org.w3.banana.sesame

import org.w3.banana.RDFGraphQueryTest

class SesameGraphQueryTest extends RDFGraphQueryTest[Sesame, SesameSPARQL](
    SesameOperations,
    SesameDiesel,
    SesameRDFXMLReader,
    SesameGraphIsomorphism,
    SesameSPARQLOperations,
    SesameGraphQuery)

