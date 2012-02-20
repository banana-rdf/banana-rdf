/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */

package org.w3.rdf

import org.w3.rdf.SimpleModule
import org.w3.rdf.NTriplesSpec

/**
 *
 * @author bblfish
 * @created 08/02/2012
 */
object SimpleNTriplesParserSpec extends NTriplesSpec(SimpleModule)

object SimpleTurtleParserSpec extends TurtleSpec(SimpleModule)