package org.w3.banana.jena

import org.w3.banana.{SparqlAnswerJson, RDFGraphQueryTest}


import org.w3.banana.jena.JenaRDFReader._
import org.w3.banana.jena.SparqlQueryResultsReader._
import org.w3.banana.jena.SparqlSolutionsWriter._

class JenaGraphQueryTest extends RDFGraphQueryTest[Jena, JenaSPARQL, SparqlAnswerJson]
