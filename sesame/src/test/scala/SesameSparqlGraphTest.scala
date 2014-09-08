package org.w3.banana.sesame

import org.w3.banana.{ SparqlAnswerJson, SparqlAnswerXml, SparqlGraphTest }
import org.w3.banana.sesame.Sesame._

class SesameSparqlGraphTestXML extends SparqlGraphTest[Sesame, SparqlAnswerXml]

class SesameSparqlGraphTestJSON extends SparqlGraphTest[Sesame, SparqlAnswerJson]
