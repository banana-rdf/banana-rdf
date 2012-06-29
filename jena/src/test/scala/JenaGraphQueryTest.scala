package org.w3.banana.jena

import org.w3.banana.{ SparqlAnswerXML, SparqlAnswerJson, RDFGraphQueryTest }

import org.w3.banana.jena.JenaRDFReader._
import org.w3.banana.jena.SparqlQueryResultsReader.{ XMLSparqlQueryResultsReader, JsonSparqlQueryResultsReader }
import org.w3.banana.jena.SparqlSolutionsWriter._

class JenaGraphQueryTestJson extends RDFGraphQueryTest[Jena, JenaSPARQL, SparqlAnswerJson]

class JenaGraphQueryTestXml extends RDFGraphQueryTest[Jena, JenaSPARQL, SparqlAnswerXML]
