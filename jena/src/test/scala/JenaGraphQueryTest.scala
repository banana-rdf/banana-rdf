package org.w3.banana.jena

import org.w3.banana._
import org.w3.banana.jena.Jena._

class JenaGraphQueryTestJson extends RDFGraphQueryTest[Jena, SparqlAnswerJson]

class JenaGraphQueryTestXml extends RDFGraphQueryTest[Jena, SparqlAnswerXml]
