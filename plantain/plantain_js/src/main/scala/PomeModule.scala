package org.w3.banana.pome

import org.w3.banana._

trait PomeModule
    extends RDFModule
    with RDFOpsModule
    with RecordBinderModule {
  //with TurtleReaderModule
  //with TurtleWriterModule {

  type Rdf = Pome

  implicit val ops: RDFOps[Pome] = PomeOps

  implicit val recordBinder: binder.RecordBinder[Pome] = binder.RecordBinder[Pome]

  //  implicit val TurtleReader: RDFReader[Plantain, Turtle] = PlantainTurtleReader

  //  implicit val TurtleWriter: RDFWriter[Plantain, Turtle] = PlantainTurtleWriter

}
