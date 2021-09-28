package org.w3.banana

import types.rdflib

object testRdfLib:
	val bblUS = "https://bblfish.net/people/henry/card#me"

	def main(args: Array[String]): Unit =
		val dp = rdflib.uriMod.docpart(bblUS)
		println(s"docpart($bblUS)=$dp")
		val hp = rdflib.uriMod.hostpart(bblUS)
		println(s"hostpart($bblUS)=$hp")
		println("document="+rdflib.uriMod.document(bblUS))
		println("protocol="+rdflib.uriMod.protocol(bblUS))
		println("refTo="+rdflib.uriMod.refTo("https://bblfish.net/",bblUS))
		println("join('./doc/cat',bblUS)="+ rdflib.uriMod.join("doc/cats.jpg",bblUS))

		rdflib.statementMod()
		println("")




