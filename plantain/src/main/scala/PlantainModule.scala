package org.w3.banana.plantain

import org.w3.banana._

trait PlantainModule
    extends RDFModule
    with RDFOpsModule
    with RecordBinderModule
    with TurtleReaderModule
    with TurtleWriterModule
    with RDFXMLReaderModule
    with SparqlOpsModule
    with SparqlGraphModule
    with XmlSolutionsWriterModule
    with JsonSolutionsWriterModule
    with XmlQueryResultsReaderModule
    with JsonQueryResultsReaderModule {

  type Rdf = Plantain

  implicit val ops: RDFOps[Plantain] = PlantainOps

  implicit val recordBinder: binder.RecordBinder[Plantain] = binder.RecordBinder[Plantain]

  implicit val turtleReader: RDFReader[Plantain, Turtle] = PlantainTurtleReader

  implicit val turtleWriter: RDFWriter[Plantain, Turtle] = PlantainTurtleWriter

  implicit val rdfXMLReader: RDFReader[Plantain, RDFXML] = PlantainRDFXMLReader

  implicit val sparqlOps: SparqlOps[Plantain] = PlantainSparqlOps

  implicit val sparqlGraph: SparqlEngine[Plantain, Plantain#Graph] = PlantainGraphSparqlEngine()

  implicit val xmlSolutionsWriter: SparqlSolutionsWriter[Plantain, SparqlAnswerXml] =
    PlantainSolutionsWriter.solutionsWriterXml

  implicit val jsonSolutionsWriter: SparqlSolutionsWriter[Plantain, SparqlAnswerJson] =
    PlantainSolutionsWriter.solutionsWriterJson

  implicit val xmlQueryResultsReader: SparqlQueryResultsReader[Plantain, SparqlAnswerXml] =
    PlantainQueryResultsReader.queryResultsReaderXml

  implicit val jsonQueryResultsReader: SparqlQueryResultsReader[Plantain, SparqlAnswerJson] =
    PlantainQueryResultsReader.queryResultsReaderJson

}
