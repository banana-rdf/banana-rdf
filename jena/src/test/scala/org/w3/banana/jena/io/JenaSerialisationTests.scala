package org.w3.banana.jena.io

import org.w3.banana.jena.JenaRdf.R
import org.w3.banana.io.{AbsoluteRDFReader, NTriples}

import scala.util.Try

//todo: move this to the library
given gg: AbsoluteRDFReader[R,Try, NTriples] = org.w3.banana.io.NTriplesReader[R]

class JenaNTripleReaderTests extends org.w3.banana.io.NTriplesReaderTests[R]