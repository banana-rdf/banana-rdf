package org.w3.banana

trait RDFDSL[Rdf <: RDF] { this: RDFOps[Rdf] =>

  // graph

  trait GraphCompanionObject {
    def empty: Rdf#Graph = emptyGraph
    def apply(elems: Rdf#Triple*): Rdf#Graph = makeGraph(elems.toIterable)
    def apply(it: Iterable[Rdf#Triple]): Rdf#Graph = makeGraph(it)
  }

  object Graph extends GraphCompanionObject

  // triple

  trait TripleCompanionObject extends Function3[Rdf#Node, Rdf#URI, Rdf#Node, Rdf#Triple] {
    def apply(s: Rdf#Node, p: Rdf#URI, o: Rdf#Node): Rdf#Triple = makeTriple(s, p, o)
    def unapply(triple: Rdf#Triple): Option[(Rdf#Node, Rdf#URI, Rdf#Node)] = Some(fromTriple(triple))
  }

  object Triple extends TripleCompanionObject

  // URI

  trait URICompanionObject extends Function1[String, Rdf#URI] {
    def apply(s: String): Rdf#URI = makeUri(s)
    def unapply(uri: Rdf#URI): Option[String] = Some(fromUri(uri))
  }

  object URI extends URICompanionObject

  // bnode

  trait BNodeCompanionObject extends Function1[String, Rdf#BNode] with Function0[Rdf#BNode] {
    def apply(): Rdf#BNode = makeBNode()
    def apply(s: String): Rdf#BNode = makeBNodeLabel(s)
    def unapply(bn: Rdf#BNode): Option[String] = Some(fromBNode(bn))
  }

  object BNode extends BNodeCompanionObject

  // alternatives fr building bnodes
  def bnode(): Rdf#BNode = BNode()
  def bnode(s: String): Rdf#BNode = BNode(s)

  // typed literal

  trait TypedLiteralCompanionObject extends Function2[String, Rdf#URI, Rdf#TypedLiteral] with Function1[String, Rdf#TypedLiteral] {
    def unapply(tl: Rdf#TypedLiteral): Option[(String, Rdf#URI)] = Some(fromTypedLiteral(tl))
    def apply(lexicalForm: String, datatype: Rdf#URI): Rdf#TypedLiteral = makeTypedLiteral(lexicalForm, datatype)
    def apply(lexicalForm: String): Rdf#TypedLiteral = makeTypedLiteral(lexicalForm, makeUri("http://www.w3.org/2001/XMLSchema#string"))
  }

  object TypedLiteral extends TypedLiteralCompanionObject

  // lang literal

  trait LangLiteralCompanionObject extends Function2[String, Rdf#Lang, Rdf#LangLiteral] {
    def apply(lexicalForm: String, lang: Rdf#Lang): Rdf#LangLiteral = makeLangLiteral(lexicalForm, lang)
    def unapply(ll: Rdf#LangLiteral): Option[(String, Rdf#Lang)] = Some(fromLangLiteral(ll))
  }

  object LangLiteral extends LangLiteralCompanionObject

  // lang

  trait LangCompanionObject extends Function1[String, Rdf#Lang] {
    def apply(s: String): Rdf#Lang = makeLang(s)
    def unapply(l: Rdf#Lang): Option[String] = Some(fromLang(l))
  }

  object Lang extends LangCompanionObject

  // graph traversal

  def getObjects(graph: Rdf#Graph, subject: Rdf#Node, predicate: Rdf#URI): Iterable[Rdf#Node] =
    find(graph, toConcreteNodeMatch(subject), toConcreteNodeMatch(predicate), ANY).map(t => fromTriple(t)._3).toIterable

  def getPredicates(graph: Rdf#Graph, subject: Rdf#Node): Iterable[Rdf#URI] =
    find(graph, toConcreteNodeMatch(subject), ANY, ANY).map(t => fromTriple(t)._2).toIterable

  def getSubjects(graph: Rdf#Graph, predicate: Rdf#URI, obj: Rdf#Node): Iterable[Rdf#Node] =
    find(graph, ANY, toConcreteNodeMatch(predicate), toConcreteNodeMatch(obj)).map(t => fromTriple(t)._1).toIterable

  // TripleMatch

  implicit def tripleAsTripleMatch(triple: Rdf#Triple): TripleMatch[Rdf] = {
    val (s, p, o) = fromTriple(triple)
    (toConcreteNodeMatch(s), toConcreteNodeMatch(p), toConcreteNodeMatch(o))
  }

  implicit def triplesAsTripleMatches(triples: Iterable[Rdf#Triple]): Iterable[TripleMatch[Rdf]] =
    triples map { triple => tripleAsTripleMatch(triple) }

}
