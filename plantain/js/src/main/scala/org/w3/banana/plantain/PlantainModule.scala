package org.w3.banana.plantain

import org.w3.banana._
import scala.util.Try
import org.w3.banana.io._

trait PlantainModule
    extends RDFModule
    with RDFOpsModule
    with RecordBinderModule
    with NTriplesReaderModule
    with NTriplesWriterModule
  {
  //with TurtleReaderModule
  //with TurtleWriterModule {

  type Rdf = Plantain

  implicit val ops: RDFOps[Plantain] = PlantainOps

  implicit val recordBinder: binder.RecordBinder[Plantain] = binder.RecordBinder[Plantain]

  //  implicit val TurtleReader: RDFReader[Plantain, Turtle] = PlantainTurtleReader

  //  implicit val TurtleWriter: RDFWriter[Plantain, Turtle] = PlantainTurtleWriter
  implicit val ntriplesReader: RDFReader[Plantain, Try, NTriples] = new NTriplesReader[Plantain]

  implicit val ntriplesWriter: RDFWriter[Plantain, Try, NTriples] = new NTriplesWriter[Plantain]

}
