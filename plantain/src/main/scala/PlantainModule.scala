package org.w3.banana.plantain

import org.w3.banana._

trait PlantainModule
    extends RDFModule
    with RDFOpsModule
    with RecordBinderModule
    with TurtleReaderModule
    with TurtleWriterModule {

  type Rdf = Plantain

  implicit val ops: RDFOps[Plantain] = PlantainOps

  implicit val recordBinder: binder.RecordBinder[Plantain] = binder.RecordBinder[Plantain]

  implicit val turtleReader: RDFReader[Plantain, Turtle] = PlantainTurtleReader

  implicit val turtleWriter: RDFWriter[Plantain, Turtle] = PlantainTurtleWriter

}
