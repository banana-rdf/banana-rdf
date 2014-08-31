package org.w3.banana

trait RDFModule {

  type Rdf <: RDF

}

trait RDFOpsModule extends RDFModule {

  implicit val Ops: RDFOps[Rdf]

}

trait RecordBinderModule extends RDFModule {

  implicit val RecordBinder: binder.RecordBinder[Rdf]

}

trait SparqlOpsModule extends RDFModule {

  implicit val SparqlOps: SparqlOps[Rdf]

}

trait SparqlGraphModule extends RDFModule {

  implicit val sparqlGraph: SparqlEngine[Rdf, Rdf#Graph]

}

trait SparqlHttpModule extends RDFModule {

  import java.net.URL

  implicit val sparqlHttp: SparqlEngine[Rdf, URL]

}

trait RDFXMLReaderModule extends RDFModule {

  implicit val RDFXMLReader: RDFReader[Rdf, RDFXML]

}

trait TurtleReaderModule extends RDFModule {

  implicit val TurtleReader: RDFReader[Rdf, Turtle]

}

trait ReaderSelectorModule extends RDFModule {

  implicit val ReaderSelector: ReaderSelector[Rdf]

}

trait RDFXMLWriterModule extends RDFModule {

  implicit val RDFXMLWriter: RDFWriter[Rdf, RDFXML]

}

trait TurtleWriterModule extends RDFModule {

  implicit val TurtleWriter: RDFWriter[Rdf, Turtle]

}

trait WriterSelectorModule extends RDFModule {

  implicit val WriterSelector: RDFWriterSelector[Rdf]

}

trait JsonSolutionsWriterModule extends RDFModule {

  implicit val JsonSolutionsWriter: SparqlSolutionsWriter[Rdf, SparqlAnswerJson]

}

trait XmlSolutionsWriterModule extends RDFModule {

  implicit val XmlSolutionsWriter: SparqlSolutionsWriter[Rdf, SparqlAnswerXml]

}

trait SparqlSolutionsWriterSelectorModule extends RDFModule {

  implicit val SparqlSolutionsWriterSelector: SparqlSolutionsWriterSelector[Rdf]

}

trait JsonQueryResultsReaderModule extends RDFModule {

  implicit val JsonQueryResultsReader: SparqlQueryResultsReader[Rdf, SparqlAnswerJson]

}

trait XmlQueryResultsReaderModule extends RDFModule {

  implicit val XmlQueryResultsReader: SparqlQueryResultsReader[Rdf, SparqlAnswerXml]

}
