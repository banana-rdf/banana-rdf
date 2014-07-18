package org.w3.banana.rdfstorew

import org.w3.banana._

trait RDFStoreModule
  extends RDFModule
  with RDFOpsModule
  with RecordBinderModule {
  //with TurtleReaderModule
  //with TurtleWriterModule {

  type Rdf = RDFStore

  implicit val Ops: RDFOps[RDFStore] = RDFStoreOps

  implicit val RecordBinder: binder.RecordBinder[RDFStore] = binder.RecordBinder[RDFStore]

  //  implicit val TurtleReader: RDFReader[Plantain, Turtle] = PlantainTurtleReader

  //  implicit val TurtleWriter: RDFWriter[Plantain, Turtle] = PlantainTurtleWriter

}
