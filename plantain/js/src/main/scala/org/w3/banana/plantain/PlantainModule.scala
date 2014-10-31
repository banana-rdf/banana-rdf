package org.w3.banana.plantain

import org.w3.banana._
import org.w3.banana.io.{NTriplesReader, NTriples, RDFReader}

import scala.util.Try

trait PlantainModule
    extends RDFModule
    with RDFOpsModule
    with NTriplesReaderModule
    with RecordBinderModule {
  //with TurtleReaderModule
  //with TurtleWriterModule {

  type Rdf = Plantain

  implicit val ops: RDFOps[Plantain] = PlantainOps

  implicit val recordBinder: binder.RecordBinder[Plantain] = binder.RecordBinder[Plantain]

  implicit val ntriplesReader: RDFReader[Plantain, Try, NTriples] = new NTriplesReader

  //  implicit val TurtleReader: RDFReader[Plantain, Turtle] = PlantainTurtleReader

  //  implicit val TurtleWriter: RDFWriter[Plantain, Turtle] = PlantainTurtleWriter

}
