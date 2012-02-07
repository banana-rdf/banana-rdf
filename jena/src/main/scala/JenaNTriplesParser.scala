package org.w3.rdf.jena

import org.w3.rdf._

object JenaNTriplesParser extends NTriplesParser[JenaModel.type](JenaModel)