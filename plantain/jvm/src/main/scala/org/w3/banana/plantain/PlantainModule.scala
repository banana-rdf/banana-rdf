package org.w3.banana.plantain

import org.w3.banana._
import org.w3.banana.plantain.io.{ PlantainTurtleWriter, PlantainTurtleReader }
import org.w3.banana.io._
import scala.util.Try

trait PlantainModule
extends RDFModule
with RDFOpsModule
with RecordBinderModule
with TurtleReaderModule
with TurtleWriterModule
with NTriplesReaderModule
with NTriplesWriterModule {

  type Rdf = Plantain

  implicit val ops: RDFOps[Plantain] = PlantainOps

  implicit val recordBinder: binder.RecordBinder[Plantain] = binder.RecordBinder[Plantain]

  implicit val turtleReader: RDFReader[Plantain, Try, Turtle] = PlantainTurtleReader

  implicit val turtleWriter: RDFWriter[Plantain, Try, Turtle] = PlantainTurtleWriter

  implicit val ntriplesReader: RDFReader[Plantain, Try, NTriples] = new NTriplesReader[Plantain]

  implicit val ntriplesWriter: RDFWriter[Plantain, Try, NTriples] = new NTriplesWriter[Plantain]

}
