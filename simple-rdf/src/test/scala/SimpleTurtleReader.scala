package org.w3.banana.simple

import org.w3.banana.jena.util._
import org.w3.banana.n3.nomo.{TurtleReader, NTriplesReader}
import org.w3.banana.Turtle
import org.w3.banana.jena.JenaRDFReader

object SimpleTurtleReader extends JenaBasedReader[SimpleRDF, Turtle](SimpleRDFOps)(JenaRDFReader.TurtleReader)

object NTriplesSeqReader extends NTriplesReader(SimpleNTriplesSeqParser)
object NTriplesStringReader extends NTriplesReader(SimpleNTriplesStringParser)


//todo create a NTriplesReader
//object NomoNTriplesSeqReader extends TurtleReader(SimpleNTriplesSeqParser)
