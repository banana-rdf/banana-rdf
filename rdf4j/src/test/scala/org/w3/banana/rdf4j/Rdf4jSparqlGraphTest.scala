package org.w3.banana.rdf4j

import org.w3.banana._
import org.w3.banana.io._
import org.w3.banana.rd4j.Rdf4j
import org.w3.banana.rd4j.io.Rdf4jSolutionsWriter

class Rdf4jSparqlGraphTestJson(implicit sparqlWriter: SparqlSolutionsWriter[Rdf4j, SparqlAnswerJson] = Rdf4jSolutionsWriter.solutionsWriterJson) extends SparqlGraphTest[Rdf4j, SparqlAnswerJson]

class Rdf4jSparqlGraphTestXml(implicit sparqlWriter: SparqlSolutionsWriter[Rdf4j, SparqlAnswerXml] = Rdf4jSolutionsWriter.solutionsWriterXml) extends SparqlGraphTest[Rdf4j, SparqlAnswerXml]
