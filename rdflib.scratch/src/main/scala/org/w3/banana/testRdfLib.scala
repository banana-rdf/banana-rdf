package org.w3.banana

import types.rdflib
import types.rdflib.{mod, namedNodeMod}
import types.rdflib.mod.Statement
import types.rdflib.tfTypesMod.{Quad, QuadGraph, QuadObject, QuadPredicate, QuadSubject}
import types.rdflib.typesMod.{GraphType, ObjectType, PredicateType, SubjectType}
import types.rdflib.typesMod.TermType.NamedNode


object testRdfLib:
	val bblUS = "https://bblfish.net/people/henry/card#me"

	val rdf: types.rdflib.typesMod.IRDFlibDataFactory = types.rdflib.rdflibDataFactoryMod.default
	val rdfDF: types.rdflib.tfTypesMod.RdfJsDataFactory = rdf.asInstanceOf[types.rdflib.tfTypesMod.RdfJsDataFactory]


	def foaf(tag: String): String = "http://xmlns.com/foaf/0.1/"+tag

	type libNode = types.rdflib.nodeInternalMod.Node
	import types.rdflib.tfTypesMod as rdfTp


	def main(args: Array[String]): Unit =
		val dp = rdflib.uriMod.docpart(bblUS)
		println(s"docpart($bblUS)=$dp")
		val hp = rdflib.uriMod.hostpart(bblUS)
		println(s"hostpart($bblUS)=$hp")
		println("document="+rdflib.uriMod.document(bblUS))
		println("protocol="+rdflib.uriMod.protocol(bblUS))
		println("refTo="+rdflib.uriMod.refTo("https://bblfish.net/",bblUS))
		println("join('./doc/cat',bblUS)="+ rdflib.uriMod.join("doc/cats.jpg",bblUS))

		import types.rdflib.namedNodeMod.NamedNode
		val bbl: namedNodeMod.default = new types.rdflib.namedNodeMod.default("https://bblfish.net/#i")
		println("bbl is mod.Subj="+ types.rdflib.mod.isSubject(bbl))
		println("bbl is mod.RdfObj="+types.rdflib.mod.isRDFObject(bbl))
		println("bbl is mod.Pred="+types.rdflib.mod.isPredicate(bbl))
		println("bbl is termsMod.Subj="+ types.rdflib.termsMod.isSubject(bbl))
		println("bbl is termsMod.RdfObj="+types.rdflib.termsMod.isRDFObject(bbl))
		println("bbl is termsMod.Pred="+types.rdflib.termsMod.isPredicate(bbl))

		val fname: namedNodeMod.default = new types.rdflib.namedNodeMod.default("http://xmlns.com/foaf/0.1/name")
		val fHmePg: namedNodeMod.default = new types.rdflib.namedNodeMod.default("http://xmlns.com/foaf/0.1/homePage")
		val fKnows: namedNodeMod.default = new types.rdflib.namedNodeMod.default("http://xmlns.com/foaf/0.1/knows")
		val name: mod.Literal = types.rdflib.mod.Literal("Henry")
		val bbl2: namedNodeMod.default = new types.rdflib.namedNodeMod.default("https://bblfish.net/#i")

		println("bbl = bbl2 = "+bbl.equals(bbl2))
		val bblf1: rdfTp.Term  = rdfDF.namedNode("https://bblfish.net/#i")
		val bblf2: rdfTp.Term  = rdf.namedNode("https://bblfish.net/#i")
		println("bblf = bblf2 = "+bblf1.equals(bblf2))
		println("bblf2 = bblf = "+bblf2.equals(bblf1))
		println("bblf1 = bblf2 = "+ (bblf1 == bblf2))
		println("bblf2 = bblf = "+ (bblf2 == bblf1))
		println("bblf1 != bblf2 = "+ (bblf1 != bblf2))
		println("bblf2 != bblf = "+ (bblf2 != bblf1))


//		type St = types.rdflib.statementMod.default[SubjectType, PredicateType, ObjectType, GraphType]
//		val st1: St = new types.rdflib.statementMod.default(bbl,fname,name)
//		println("st1.subject is Subj="+ types.rdflib.mod.isSubject(st1.subject))
//		println("st1.subject is RdfObj="+types.rdflib.mod.isRDFObject(st1.subject))
//		println("st1.subject is Pred="+types.rdflib.mod.isPredicate(st1.subject))
//		val st2: St = new types.rdflib.statementMod.default(bbl,fHmePg,rdflib.uriMod.document(bblUS))
//		val b1: mod.BlankNode = types.rdflib.mod.BlankNode("a")
//		val st3: St = new types.rdflib.statementMod.default(bbl,fKnows,b1)
//		val st4: St = new types.rdflib.statementMod.default(b1,fname,types.rdflib.mod.Literal("Gordana"))
//		val stLst = List(st1,st2,st3,st4)
//		stLst.foreach(println(_))
//		println(st1.graph)
//		import scala.scalajs.js.undefined
//		val s: types.rdflib.storeMod.IndexedFormula = new types.rdflib.mod.Store()
//		s.add(bbl.asInstanceOf[types.rdflib.tfTypesMod.QuadSubject],fKnows.asInstanceOf[types.rdflib.tfTypesMod.QuadPredicate],
//			bbl.asInstanceOf[types.rdflib.tfTypesMod.Term],undefined)
//		println("hello")
//		println(s)
//		stLst.foreach{ st=>
//			println(st)
//			s.add(
//				st.subject.asInstanceOf[types.rdflib.tfTypesMod.QuadSubject],
//				st.predicate.asInstanceOf[types.rdflib.tfTypesMod.QuadPredicate],
//				st.`object`.asInstanceOf[types.rdflib.tfTypesMod.Term],
//				undefined
//			)
//		}
//		println("Store="+s)
////		s.query()
////		println(types.rdflib.serializeMod.default(s, undefined, undefined,undefined,undefined,undefined))
////		val st2 = types.rdflib.statementMod.default(bbl,knows,)
//
//		println("-----------------------")
//		println()
//
//		val rdf: types.rdflib.typesMod.IRDFlibDataFactory = types.rdflib.rdflibDataFactoryMod.default
//		val lh = rdf.lit("Henry")
//		val bblf = rdf.namedNode("https://bblfish.net/#i")
//		val bblHm = rdf.namedNode("https://bblfish.net/")
//		val flk = rdf.namedNode(foaf("knows"))
//		val fHmp = rdf.namedNode(foaf("homePage"))
//		val fnm = rdf.namedNode(foaf("name"))
//		val stm1 = rdf.st(bblf,fHmePg,bblHm)
//		println(stm1)
//		val stm2 = rdf.st(bblf,fnm,lh)
//		val stm2x = rdf.st(bblf,fnm,lh)
//		println(s"$st1 == $stm2 ? answer "+(stm1.equals(stm2)))
//		println(s"stm2 == stm2x ? answer "+(stm2.equals(stm2x)))
////		val store: types.rdflib.storeMod.default = rdf.graph()
////		store.add(stm1)
////		store.add(stm2)
////		println(store)
//
//		val rdf2: types.rdflib.tfTypesMod.RdfJsDataFactory = types.rdflib.rdflibDataFactoryMod.default
//		val lh2 = rdf2.literal("Henry","")
//		val bblf2 = rdf2.namedNode("https://bblfish.net/#i")
//		val bblHm2 = rdf2.namedNode("https://bblfish.net/")
//		val flk2 = rdf2.namedNode(foaf("knows"))
//		val fHmp2 = rdf2.namedNode(foaf("homePage"))
//		val fnm2 = rdf2.namedNode(foaf("name"))
//		val stm12 = rdf2.triple(bblf2,fHmp2,bblHm2)
//		println(stm12)
//		val stm22 = rdf2.triple(bblf2,fnm2,lh2).asInstanceOf[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]]
//		val stm2x2 = rdf2.triple(bblf2,fnm2,lh2).asInstanceOf[Quad[QuadSubject, QuadPredicate, QuadObject, QuadGraph]]
//		println(s"$stm12 == $stm22 ? answer "+(stm12.equals(stm22)))
//		println(s"stm22 == stm2x2 ? answer "+(rdf.equals(stm22,stm2x2)))
////		val store: types.rdflib.storeMod.default = rdf.graph()





