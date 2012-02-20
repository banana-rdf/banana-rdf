package org.w3.rdf.jena

import org.w3.rdf

class JenaTurtleTest extends rdf.TurtleTestSuite(JenaModule, TurtleReader, GraphIsomorphism)