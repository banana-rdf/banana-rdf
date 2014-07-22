package org.w3.banana.rdfstorew

import scala.scalajs.js
import scala.scalajs.js.Dynamic.global
import akka.http.model.{IllegalUriException, Uri}

import org.w3.banana.{URIOps, RDFOps}

trait JSUtils {
  def log(obj:RDFStoreRDFNode) = js.Dynamic.global.console.log(obj.jsNode)
  def log(obj:RDFStoreTriple) = js.Dynamic.global.console.log(obj.triple)
  def log(obj:RDFStoreGraph) = js.Dynamic.global.console.log(obj.graph)
  def log(obj:js.Dynamic) = {
    js.Dynamic.global.console.log(obj)
  }
  def log(obj:String) = js.Dynamic.global.console.log(obj)

  def global = js.Dynamic.global
}

trait RDFStoreURIOps extends URIOps[RDFStore] {

  def akka(uri:RDFStore#URI) : Uri = Uri(uri.valueOf)

  def rdfjs(uri:Uri) : RDFStore#URI = {
    RDFStoreOps.makeUri(uri.toString)
  }

  def getString(uri: RDFStore#URI): String = akka(uri).toString

  def withoutFragment(uri: RDFStore#URI): RDFStore#URI =  {
    RDFStoreOps.makeUri(uri.valueOf.split("#")(0))
  }

  def withFragment(uri: RDFStore#URI, frag: String): RDFStore#URI = {
    rdfjs(akka(uri).withFragment(frag))
  }

  def getFragment(uri: RDFStore#URI): Option[String] = akka(uri).fragment

  def isPureFragment(uri: RDFStore#URI): Boolean = {
    val u = akka(uri)
    u.scheme.isEmpty && u.authority.isEmpty && u.path.isEmpty && u.query.isEmpty && u.fragment.isDefined
  }



  def resolve(uri: RDFStore#URI, other: RDFStore#URI): RDFStore#URI =
    rdfjs(akka(other).resolvedAgainst(akka(uri).toString))

  def appendSegment(uri: RDFStore#URI, segment: String): RDFStore#URI = {
    val underlying = akka(uri)
    val path = underlying.path
    if (path.reverse.startsWithSlash)
      rdfjs(underlying.copy(path = path + segment))
    else
      rdfjs(underlying.copy(path = path / segment))
  }

  def relativize(uri: RDFStore#URI, other: RDFStore#URI): RDFStore#URI = ???

  def newChildUri(uri: RDFStore#URI): RDFStore#URI = {
    val segment = java.util.UUID.randomUUID().toString.replace("-", "")
    appendSegment(uri, segment)
  }

  def lastSegment(uri: RDFStore#URI): String = akka(uri).path.reverse.head.toString

}

object RDFStoreOps extends RDFOps[RDFStore] with RDFStoreURIOps with JSUtils {

  override def emptyGraph: RDFStore#Graph = new RDFStoreGraph(RDFStoreW.rdf.createGraph())

  override implicit def toConcreteNodeMatch(node: RDFStore#Node): RDFStore#NodeMatch = PlainNode(node)

  override def diff(g1: RDFStore#Graph, g2: RDFStore#Graph): RDFStore#Graph = ???

  override def fromTriple(triple: RDFStore#Triple): (RDFStore#Node, RDFStore#URI, RDFStore#Node) = (triple.subject, triple.predicate, triple.objectt)

  override def makeBNode(): RDFStore#BNode = new RDFStoreBlankNode(RDFStoreW.rdf.createBlankNode())

  override def graphToIterable(graph: RDFStore#Graph): Iterable[RDFStore#Triple] = graph.triples.asInstanceOf[js.Array[RDFStore#Triple]]


  override def foldNode[T](node: RDFStore#Node)(funURI: (RDFStore#URI) => T, funBNode: (RDFStore#BNode) => T, funLiteral: (RDFStore#Literal) => T): T = ???

  override def makeLang(s: String): RDFStore#Lang = s

  override def makeBNodeLabel(s: String): RDFStore#BNode = new RDFStoreBlankNode(js.Dynamic.newInstance(RDFStoreW.rdf_api.BlankNode)(s))

  override def makeLangTaggedLiteral(lexicalForm: String, lang: RDFStore#Lang): RDFStore#Literal =
    new RDFStoreLiteral(js.Dynamic.newInstance(RDFStoreW.rdf_api.Literal)(lexicalForm,lang,null))

  override def fromLiteral(literal: RDFStore#Literal): (String, RDFStore#URI, Option[RDFStore#Lang]) = {
    val lexicalForm:String = literal.nominalValue.asInstanceOf[String]
    val datatype:RDFStore#URI = if(literal.datatype == null) { null } else { makeUri(literal.datatype) }
    var lang:Option[RDFStore#Lang] = if(literal.language == null) { None } else { Some(literal.language.asInstanceOf[RDFStore#Lang]) }

    (lexicalForm, datatype, lang)
  }

  override def makeUri(s: String): RDFStore#URI = {
    new RDFStoreNamedNode(RDFStoreW.rdf.createNamedNode(s))
  }

  override def makeTriple(s: RDFStore#Node, p: RDFStore#URI, o: RDFStore#Node): RDFStore#Triple = {
    new RDFStoreTriple(RDFStoreW.rdf.createTriple(s.jsNode,p.jsNode,o.jsNode))
  }

  override def ANY: RDFStore#NodeAny = JsANY

  override def makeGraph(it: Iterable[RDFStore#Triple]): RDFStore#Graph = {
    var triplesArray = js.Dynamic.newInstance(global.Array)()
    for(triple <- it) {
      triplesArray.push(triple.triple)
    }

    new RDFStoreGraph(RDFStoreW.rdf.createGraph(triplesArray))
  }

  override def fromLang(l: RDFStore#Lang): String = l

  override def foldNodeMatch[T](nodeMatch: RDFStore#NodeMatch)(funANY: => T, funNode: (RDFStore#Node) => T): T = ???

  override def isomorphism(left: RDFStore#Graph, right: RDFStore#Graph): Boolean = GraphEquivalence.findAnswer(left,right).isSuccess

  override def find(graph: RDFStore#Graph, subject: RDFStore#NodeMatch, predicate: RDFStore#NodeMatch, objectt: RDFStore#NodeMatch): Iterator[RDFStore#Triple] = {

    val subjectNode:RDFStore#Node = subject match {
      case PlainNode(node) => node
      case _               => null
    }

    val predicateNode:RDFStore#Node = predicate match {
      case PlainNode(node) => node
      case _               => null
    }

    val objectNode:RDFStore#Node = subject match {
      case PlainNode(node) => node
      case _               => null
    }


    var toFind:RDFStore#Triple = makeTriple(subjectNode, predicateNode.asInstanceOf[RDFStore#URI], objectNode)
    var filtered:js.Array[js.Dynamic] = graph.graph.filter({
      (t:js.Dynamic, g:js.Dynamic) =>
        val triple = new RDFStoreTriple(t)
        t.equals(toFind)
    }).asInstanceOf[js.Array[js.Dynamic]]

    var filteredList:List[RDFStore#Triple] = List[RDFStore#Triple]()
    for(triple <- filtered) {
      filteredList = filteredList.::(new RDFStoreTriple(triple))
    }

    filteredList.toIterator
  }

  override def fromBNode(bn: RDFStore#BNode): String = bn.toString()

  override def fromUri(uri: RDFStore#URI): String = getString(uri)

  // graph union
  override def union(graphs: Seq[RDFStore#Graph]): RDFStore#Graph = graphs.fold(emptyGraph)(_ merge _)

  override def makeLiteral(lexicalForm: String, datatype: RDFStore#URI): RDFStore#Literal = {
    var value = lexicalForm
    var lang:String = null
    var datatypeString:String = null

    if(lexicalForm.indexOf("@") != -1) {
      val parts = lexicalForm.split("@")
      value = parts(0)
      lang = parts(1)
    }

    if(datatype != null) {
      datatypeString = getString(datatype)
    }

    new RDFStoreLiteral(js.Dynamic.newInstance(RDFStoreW.rdf_api.Literal)(value,lang,datatypeString))
  }
}
