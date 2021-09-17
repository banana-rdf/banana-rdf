package org.w3.banana.syntax

import scala.annotation.targetName
import org.w3.banana.{RDF,Ops}

//this can be an external import
object LiteralW:
	extension [Rdf<:RDF](str: String)(using ops: Ops[Rdf])
		@targetName("dt")
		infix def ^^(dtType: RDF.URI[Rdf]): RDF.Literal[Rdf] =
			ops.Literal.dtLiteral(str, dtType)
		@targetName("lang")
		infix def `@`(lang: RDF.Lang[Rdf]): RDF.Literal[Rdf] =
			ops.Literal.langLiteral(str, lang)

//	extension [Rdf<:RDF](lit: Literal[Rdf])(using ops: Ops[Rdf])


end LiteralW

object LangW:
	extension [Rdf<:RDF](lang: RDF.Lang[Rdf])(using ops: Ops[Rdf])
		def label: String =  ops.Lang.label(lang)

