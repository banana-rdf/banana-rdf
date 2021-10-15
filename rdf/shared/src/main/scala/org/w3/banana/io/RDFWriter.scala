package org.w3.banana
package io
import RDF.*

import java.io.Writer

/**
 * serialise an RDF Graph into a syntax S that does not admit relative URLs.
 * @tparam Rdf RDF encoding
 * @tparam M Context in which result is wropped
 * @tparam S Syntax phantom marker trait
 */
trait AbsoluteRDFWriter[Rdf <: RDF, M[_], +S]:
	/**
	 * Write the triples of a graph to an java.io.Writer
	 */
	def write(triples: Iterator[Triple[Rdf]], os: Writer): M[Unit]


/**
 * Serialise an RDF Graph into a syntax S that accepts relative URIs
 * @tparam Rdf
 * @tparam M
 * @tparam T
 */
trait RDFWriter[Rdf <: RDF, M[_], +T]:  //extends Writer[Rdf#Graph,M,T] {
	/**
	 * write out the Reltaive Triples from a graph to wr: java.io.Writer
	 * Passing the graph as an interator of Triples allows one to specify the order
	 *  of writing these out and also to relativise any URIs to be written
	 */
	def write(
		graph: Iterator[rTriple[Rdf]], wr: Writer, prefixes: Set[Prefix[Rdf]] = Set()
	): M[Unit]

