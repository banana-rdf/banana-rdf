package org.w3.banana.jena

import org.w3.banana._

import org.w3.banana.jena.Jena._
import org.w3.banana.jena.SparqlQueryResultsReader.{ XMLSparqlQueryResultsReader, JsonSparqlQueryResultsReader }
import org.w3.banana.jena.SparqlSolutionsWriter._
import JenaStore._

class JenaGraphQueryTestJson extends RDFGraphQueryTest[Jena, SparqlAnswerJson]

class JenaGraphQueryTestXml extends RDFGraphQueryTest[Jena, SparqlAnswerXML]
