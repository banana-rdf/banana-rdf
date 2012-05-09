package org.w3.banana.simple

import org.w3.banana.jena.util._
import org.w3.banana.n3.nomo.{TurtleReader, NTriplesReader}

object SimpleTurtleReader extends JenaBasedTurtleReader[SimpleRDF](SimpleRDFOperations)

object NTriplesSeqReader extends NTriplesReader(SimpleNTriplesSeqParser)
object NTriplesStringReader extends NTriplesReader(SimpleNTriplesStringParser)


//todo create a NTriplesReader
//object NomoNTriplesSeqReader extends TurtleReader(SimpleNTriplesSeqParser)
