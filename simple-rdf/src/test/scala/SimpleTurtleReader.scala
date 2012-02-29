package org.w3.rdf.simple

import org.w3.rdf.util.DefaultTurtleReader
import org.w3.rdf.n3.nomo.{TurtleReader, NTriplesReader}

object SimpleTurtleReader extends DefaultTurtleReader(SimpleRDFOperations)

object NTriplesSeqReader extends NTriplesReader(SimpleNTriplesSeqParser)
object NTriplesStringReader extends NTriplesReader(SimpleNTriplesStringParser)

//this is kind of useful enough to be in the n3 lib
object TurtleSeqReader extends TurtleReader(TurtleSeqParser)
