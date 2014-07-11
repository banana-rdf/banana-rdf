package org.w3.banana.pome

import org.w3.banana._

trait PlantainModule
extends RDFModule
with RDFOpsModule
with RecordBinderModule {
//with TurtleReaderModule
//with TurtleWriterModule {

  type Rdf = Plantain

  implicit val Ops: RDFOps[Plantain] = PlantainOps

  implicit val RecordBinder: binder.RecordBinder[Plantain] = binder.RecordBinder[Plantain]

//  implicit val TurtleReader: RDFReader[Plantain, Turtle] = PlantainTurtleReader

//  implicit val TurtleWriter: RDFWriter[Plantain, Turtle] = PlantainTurtleWriter

}
