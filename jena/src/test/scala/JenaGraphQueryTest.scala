package org.w3.banana.jena

import org.w3.banana._
import Jena._
import JenaRDFReader._
import SparqlSolutionsReader._
import SparqlSolutionsWriter._

class JenaGraphQueryTest extends RDFGraphQueryTest[Jena, JenaSPARQL, SparqlAnswerJson]
