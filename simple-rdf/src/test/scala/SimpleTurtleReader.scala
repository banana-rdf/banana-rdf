package org.w3.rdf.simple

import org.w3.rdf.util.DefaultTurtleReader
import org.w3.rdf.n3.nomo.NTriplesReader

object SimpleTurtleReader extends DefaultTurtleReader(SimpleRDFOperations)

object NTriplesSeqReader extends NTriplesReader(SimpleNTriplesSeqParser)
object NTriplesStringReader extends NTriplesReader(SimpleNTriplesStringParser)
