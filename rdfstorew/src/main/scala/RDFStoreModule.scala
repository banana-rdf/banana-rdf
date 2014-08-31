package org.w3.banana.rdfstorew

import org.w3.banana._

trait RDFStoreModule
    extends RDFModule
    with RDFOpsModule
    with RecordBinderModule
    with TurtleReaderModule
    with TurtleWriterModule {

  type Rdf = RDFStore

  implicit val Ops: RDFStoreOps = new RDFStoreOps

  implicit val SparqlOps: SparqlOps[RDFStore] = RDFSparqlOps

  implicit val RecordBinder: binder.RecordBinder[RDFStore] = binder.RecordBinder[RDFStore]

  implicit val TurtleReader: RDFReader[RDFStore, Turtle] = new RDFStoreTurtleReader

  implicit val TurtleWriter: RDFWriter[RDFStore, Turtle] = RDFStoreTurtleWriter

}
