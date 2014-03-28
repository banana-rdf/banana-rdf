package org.w3.banana.sesame

import org.w3.banana._

trait SesameModule
extends RDFModule
with RDFOpsModule
with RecordBinderModule
with SparqlGraphModule
// with SparqlHttpModule
with RDFXMLReaderModule
with TurtleReaderModule
// with ReaderSelectorModule
with RDFXMLWriterModule
with TurtleWriterModule
// with WriterSelectorModule
with JsonSolutionsWriterModule
with XmlSolutionsWriterModule
with JsonQueryResultsReaderModule
with XmlQueryResultsReaderModule {

  type Rdf = Sesame

  implicit val Ops: RDFOps[Sesame] = SesameOperations

  implicit val RecordBinder: binder.RecordBinder[Sesame] = binder.RecordBinder[Sesame]

  implicit val SparqlOps: SparqlOps[Sesame] = SesameSparqlOps

  implicit val SparqlGraph: SparqlGraph[Sesame] = SesameSparqlGraph

  implicit val RDFXMLReader: RDFReader[Sesame, RDFXML] = SesameRDFXMLReader

  implicit val TurtleReader: RDFReader[Sesame, Turtle] = SesameTurtleReader

  implicit val RDFXMLWriter: RDFWriter[Sesame, RDFXML] = SesameRDFWriter.rdfxmlWriter

  implicit val TurtleWriter: RDFWriter[Sesame, Turtle] = SesameRDFWriter.turtleWriter

  implicit val JsonSolutionsWriter: SparqlSolutionsWriter[Sesame, SparqlAnswerJson] =
    SesameSolutionsWriter.solutionsWriterJson

  implicit val XmlSolutionsWriter: SparqlSolutionsWriter[Sesame, SparqlAnswerXml] =
    SesameSolutionsWriter.solutionsWriterXml

  implicit val JsonQueryResultsReader: SparqlQueryResultsReader[Sesame, SparqlAnswerJson] =
    SesameQueryResultsReader.queryResultsReaderJson

  implicit val XmlQueryResultsReader: SparqlQueryResultsReader[Sesame, SparqlAnswerXml] =
    SesameQueryResultsReader.queryResultsReaderXml

}
