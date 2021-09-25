package org.w3.banana.io

import org.w3.banana.{RDF,Ops}
import RDF.*

import java.io.Writer

import scala.util.Try

/**
 * Generic NTriplesWriter
 * @param ops implicit Rdf operations, that by default are resolved from Rdf typeclass
 * @tparam Rdf class with Rdf types
 */
class NTriplesWriter[Rdf <: RDF](using val ops: Ops[Rdf])
	extends AbsoluteRDFWriter[Rdf,Try,NTriples]  {

	import ops.{given,*}

	protected def tripleAsString(t: Triple[Rdf]): String = {
		node2Str(t.subj)+" "+node2Str(t.rel)+" "+node2Str(t.obj)+" ."
	}

	/**
	 * Translates node to its ntriples string representation
	 * @param node Rdf node
	 * @return
	 */
	def node2Str(node: Node[Rdf]): String = node.fold(
		bn => "_:" + bn.label,
		url => "<" + url.string + ">",
		lit => lit.fold(
			txt => "\"" + txt + "\"",
			(txt,lang) => "\"" + txt + "\"" + "@" + lang.label,
			(txt,tp) => "\"" + txt + "\"" + "^^<" + tp.string + ">"
		)
	)

	override
	def write(graph: Iterator[Triple[Rdf]], wr: Writer): Try[Unit] = Try {
		for (triple <- graph) {
			val line = tripleAsString(triple) + "\r\n"
			wr.write(line)
		}
	}

}