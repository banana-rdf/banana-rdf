package org.w3.banana.jena

import org.w3.banana._
import org.w3.banana.jena.Jena._

class SparqlGraphTestJson extends SparqlGraphTest[Jena, SparqlAnswerJson]

class SparqlGraphTestXml extends SparqlGraphTest[Jena, SparqlAnswerXml]
