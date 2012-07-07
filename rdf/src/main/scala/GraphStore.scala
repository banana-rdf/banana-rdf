package org.w3.banana

import scalaz.Id

/**
 * to manipulate named graphs
 */
trait GraphStore[Rdf <: RDF] extends MGraphStore[Rdf, Id]
