package org.w3.banana.rdflib

import org.w3.banana.rdflib.storeMod.IndexedFormula
import run.cosy.rdfjs.model
import run.cosy.rdfjs.model.{DataFactory, Literal, NamedNode, Quad, Term, ValueTerm}

import scala.scalajs.js
import scala.scalajs.js.{ThisFunction3, ThisFunction4, |}
import scala.scalajs.js.annotation.{JSImport, JSName}

object mod {


	@JSImport("rdflib", JSImport.Namespace)
	@js.native
	val ^ : js.Any = js.native

	inline def isRDFlibObject(obj: js.Any): /* is rdflib.rdflib/lib/types.ObjectType */ Boolean = ^.asInstanceOf[js.Dynamic].applyDynamic("isRDFlibObject")(obj.asInstanceOf[js.Any]).asInstanceOf[/* is rdflib.rdflib/lib/types.ObjectType */ Boolean]

}

object Test {

	val addStatement:  js.ThisFunction1[IndexedFormula,Quad, Quad | Null] =
		(thisFrmla: IndexedFormula, quad: Quad) =>
			println("in my scala addStatement method")
			val predHash = thisFrmla.rdfFactory.id(quad.rel)
			val actions = thisFrmla.propertyActions.get(predHash).getOrElse(js.Array())
			import quad.*
			for act <- actions do
				act(thisFrmla,subj,rel,obj,graph)
			if thisFrmla.holdsStatement(quad) then null
			else
				val hash: js.Array[String] = js.Array(
					thisFrmla.id(subj),predHash,
					thisFrmla.id(obj),thisFrmla.id(graph))
				val indexArr = thisFrmla.index.asInstanceOf[js.Array[thisFrmla.Index]]
				hash.zip(indexArr).foreach{ case (h, ix) =>
					ix.getOrElseUpdate(h,js.Array[Quad]()).append(quad)
				}
				//would be faster with a hash map!!
				thisFrmla.statements.push(quad)
				for cb <- thisFrmla.dataCallbacks.getOrElse(js.Array()) do cb(quad)
				quad


	val add : js.ThisFunction4[IndexedFormula,
		Quad.Subject | Quad | js.Array[Quad],
		js.UndefOr[Quad.Predicate],
		js.UndefOr[Quad.Object],
		js.UndefOr[Quad.Graph],
		IndexedFormula|Quad|Null
	] =
		(thisArg: IndexedFormula,
		arg1: Quad.Subject | Quad | js.Array[Quad],
		arg2: js.UndefOr[Quad.Predicate],
		arg3: js.UndefOr[Quad.Object],
		arg4: js.UndefOr[Quad.Graph]) =>
			println(s"thisArg=$thisArg arg1=$arg1 arg2=$arg2, arg3=$arg3")
			arg1 match
			case qs : js.Array[Quad] =>
				for q <- qs do thisArg.addStatement(q)
				thisArg
			case q: Quad =>
				thisArg.addStatement(q)
				thisArg
			case subj : Quad.Subject if arg2.isDefined && arg3.isDefined =>
				val q = thisArg.rdfFactory.quad(subj,arg2.get,arg3.get,arg4.getOrElse(thisArg.rdfFactory.defaultGraph))
				println(s"q=$q")
				thisArg.addStatement(q)
			case _ => throw new IllegalArgumentException(s"IndexedFormula.add($arg1,$arg2,$arg3,$arg4) has wrong arguments")

	//the rdflib code returns a Node which is a Term with extra methods, and also does something looking at redirects
	val canon: js.ThisFunction1[IndexedFormula, js.UndefOr[Term[?]],Term[?]|js.Object] =
		(ixf: IndexedFormula, term: js.UndefOr[Term[?]]) =>
			println(s"in canon($term)")
			//todo: add redirections code
			term.getOrElse(falseEquals)

	val falseEquals = new js.Object {
		@JSName("equals")
		def equalsTerm(obj: Any): Boolean = false
	}

	def main(args: Array[String]): Unit =
		val df: DataFactory = model.DataFactory()
		val nn: NamedNode = df.namedNode("https://bblfish.net/")
		val kn: NamedNode = df.namedNode("https://xmlns.com/foaf/0.1/name")
		val name: Literal = df.literal("Henry")
		println(s"$nn.termType == ${nn.termType} is "+ (nn.termType == "NamedNode"))
		println(s"is $nn an RDF Object? " + mod.isRDFlibObject(nn.asInstanceOf[js.Any]))
		val opts = FormulaOpts()
		opts.setRdfFactory(df)
		val ix = storeMod(opts)
		println(ix.bnode("hello"))
		val x0 = ix.asInstanceOf[js.Dynamic].applyDynamic("add")(nn.asInstanceOf[js.Any], kn.asInstanceOf[js.Any], name.asInstanceOf[js.Any])
		println(s"x0=$x0")
		val x : Quad = ix.add(nn,kn,name)
		println(s"x=$x")

//		println(s"is $nn an RDF Object?" + mod.isRDFlibObject(nn.asInstanceOf[js.Any]))

}
