package org.w3.banana.jena

import org.w3.banana._

trait JenaModule
    extends RDFModule
    with RDFOpsModule
    with RecordBinderModule
    with SparqlGraphModule
    with SparqlHttpModule
    with RDFXMLReaderModule
    with TurtleReaderModule
    with ReaderSelectorModule
    with RDFXMLWriterModule
    with TurtleWriterModule
    with WriterSelectorModule
    with JsonSolutionsWriterModule
    with XmlSolutionsWriterModule
    with JsonQueryResultsReaderModule
    with XmlQueryResultsReaderModule {

  type Rdf = Jena

  implicit val Ops: JenaOps = new JenaOps

  implicit val JenaUtil: JenaUtil = new JenaUtil(Ops)

  implicit val RecordBinder: binder.RecordBinder[Jena] = binder.RecordBinder[Jena]

  implicit val SparqlOps: SparqlOps[Jena] = new JenaSparqlOps(JenaUtil)

  implicit val SparqlGraph: SparqlGraph[Jena] = new JenaSparqlGraph(Ops)

  implicit val SparqlHttp: SparqlHttp[Jena] = new JenaSparqlHttp(Ops)

  implicit val RDFXMLReader: RDFReader[Jena, RDFXML] = JenaRDFReader.rdfxmlReader(Ops)

  implicit val TurtleReader: RDFReader[Jena, Turtle] = JenaRDFReader.turtleReader(Ops)

  implicit val N3Reader: RDFReader[Jena, N3] = JenaRDFReader.n3Reader(Ops)

  implicit val ReaderSelector: ReaderSelector[Jena] = JenaRDFReader.selector

  implicit val RDFXMLWriter: RDFWriter[Jena, RDFXML] = JenaRDFWriter.rdfxmlWriter

  implicit val TurtleWriter: RDFWriter[Jena, Turtle] = JenaRDFWriter.turtleWriter

  implicit val N3Writer: RDFWriter[Jena, N3] = JenaRDFWriter.n3Writer

  implicit val WriterSelector: RDFWriterSelector[Jena] = JenaRDFWriter.selector

  implicit val JsonSolutionsWriter: SparqlSolutionsWriter[Jena, SparqlAnswerJson] =
    JenaSolutionsWriter.solutionsWriterJson

  implicit val XmlSolutionsWriter: SparqlSolutionsWriter[Jena, SparqlAnswerXml] =
    JenaSolutionsWriter.solutionsWriterXml

  implicit val SparqlSolutionsWriterSelector: SparqlSolutionsWriterSelector[Jena] = JenaSolutionsWriter.solutionsWriterSelector

  implicit val JsonQueryResultsReader: SparqlQueryResultsReader[Jena, SparqlAnswerJson] =
    JenaQueryResultsReader.queryResultsReaderJson

  implicit val XmlQueryResultsReader: SparqlQueryResultsReader[Jena, SparqlAnswerXml] =
    JenaQueryResultsReader.queryResultsReaderXml

}
