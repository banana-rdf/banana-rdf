package org.w3.banana

trait RDFModule {

  type Rdf <: RDF

}

trait RDFOpsModule extends RDFModule {

  implicit val ops: RDFOps[Rdf]

}

trait RecordBinderModule extends RDFModule {

  implicit val recordBinder: binder.RecordBinder[Rdf]

}

trait SparqlOpsModule extends RDFModule {

  implicit val sparqlOps: SparqlOps[Rdf]

}

trait SparqlGraphModule extends RDFModule {

  implicit val sparqlGraph: SparqlEngine[Rdf, Rdf#Graph]

}

trait SparqlHttpModule extends RDFModule {

  import java.net.URL

  implicit val sparqlHttp: SparqlEngine[Rdf, URL]

}

trait RDFXMLReaderModule extends RDFModule {

  implicit val rdfXMLReader: RDFReader[Rdf, RDFXML]

}

trait TurtleReaderModule extends RDFModule {

  implicit val turtleReader: RDFReader[Rdf, Turtle]

}

trait ReaderSelectorModule extends RDFModule {

  implicit val readerSelector: ReaderSelector[Rdf]

}

trait RDFXMLWriterModule extends RDFModule {

  implicit val rdfXMLWriter: RDFWriter[Rdf, RDFXML]

}

trait TurtleWriterModule extends RDFModule {

  implicit val turtleWriter: RDFWriter[Rdf, Turtle]

}

trait WriterSelectorModule extends RDFModule {

  implicit val writerSelector: RDFWriterSelector[Rdf]

}

trait JsonSolutionsWriterModule extends RDFModule {

  implicit val jsonSolutionsWriter: SparqlSolutionsWriter[Rdf, SparqlAnswerJson]

}

trait XmlSolutionsWriterModule extends RDFModule {

  implicit val xmlSolutionsWriter: SparqlSolutionsWriter[Rdf, SparqlAnswerXml]

}

trait SparqlSolutionsWriterSelectorModule extends RDFModule {

  implicit val sparqlSolutionsWriterSelector: SparqlSolutionsWriterSelector[Rdf]

}

trait JsonQueryResultsReaderModule extends RDFModule {

  implicit val jsonQueryResultsReader: SparqlQueryResultsReader[Rdf, SparqlAnswerJson]

}

trait XmlQueryResultsReaderModule extends RDFModule {

  implicit val xmlQueryResultsReader: SparqlQueryResultsReader[Rdf, SparqlAnswerXml]

}
