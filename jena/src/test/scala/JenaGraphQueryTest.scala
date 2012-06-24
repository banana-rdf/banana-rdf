package org.w3.banana.jena

import org.w3.banana._
import Jena._
import JenaRDFReader._
import SparqlAnswerReader._
import SparqlAnswerWriter._

class JenaGraphQueryTest extends RDFGraphQueryTest[Jena, JenaSPARQL, SparqlAnswerJson]
