package org.w3.banana.rdfstorew

import org.w3.banana.iso.GraphIsomorphism
import org.w3.banana.{ RDFOps, URIOps }
import java.net.{ URI => jURI }

import scala.scalajs.js

trait JSUtils {
  def log(obj: RDFStoreRDFNode) = js.Dynamic.global.console.log(obj.jsNode)

  def log(obj: RDFStoreTriple) = js.Dynamic.global.console.log(obj.triple)

  def log(obj: RDFStoreGraph) = js.Dynamic.global.console.log(obj.graph)

  def log(obj: js.Dynamic) = {
    js.Dynamic.global.console.log(obj)
  }

  def log(obj: String) = js.Dynamic.global.console.log(obj)

  def global = js.Dynamic.global
}

trait RDFStoreURIOps extends URIOps[RDFStore] {

  private def java(uri: RDFStore#URI): jURI = new jURI(uri.valueOf)

  def rdfjs(uri: jURI): RDFStore#URI = {
    (new RDFStoreOps()).makeUri(uri.toString)
  }

  def getString(uri: RDFStore#URI): String = java(uri).toString

  def withoutFragment(uri: RDFStore#URI): RDFStore#URI = {
    (new RDFStoreOps()).makeUri(uri.valueOf.split("#")(0))
  }

  def withFragment(uri: RDFStore#URI, frag: String): RDFStore#URI = {
    val u = java(uri)
    import u._
    rdfjs(new jURI(getScheme, getUserInfo, getHost, getPort, getPath, getQuery, frag))
  }

  def getFragment(uri: RDFStore#URI): Option[String] = Option(java(uri).getFragment)

  def isPureFragment(uri: RDFStore#URI): Boolean = {
    val u = java(uri)
    import u.{ getFragment => fragment, _ }
    getScheme == null &&
      getUserInfo == null && getAuthority == null &&
      (getPath == null || getPath == "") &&
      getQuery == null && fragment != null

  }

  def resolve(uri: RDFStore#URI, other: RDFStore#URI): RDFStore#URI =
    rdfjs(java(uri).resolve(java(other)))

  def appendSegment(uri: RDFStore#URI, segment: String): RDFStore#URI = {
    val u = java(uri)
    val path = u.getPath
    val newpath = if (path.endsWith("/")) path + segment else path + "/" + segment
    import u._
    val res = rdfjs(new jURI(getScheme, getUserInfo, getHost, getPort, newpath, getQuery, null))
    res
  }

  def relativize(uri: RDFStore#URI, other: RDFStore#URI): RDFStore#URI = {
    val result = new jURI(uri.nominalValue).relativize(new jURI(other.nominalValue))
    (new RDFStoreOps()).makeUri(result.toString)
  }

  def lastSegment(uri: RDFStore#URI): String = {
    val path = java(uri).getPath
    val i = path.lastIndexOf('/')
    path.substring(i + 1, path.length)
  }
}

class RDFStoreOps extends RDFOps[RDFStore] with RDFStoreURIOps with JSUtils {

  override def emptyGraph: RDFStore#Graph = new RDFStoreGraph(RDFStoreW.rdf.createGraph())

  override implicit def toConcreteNodeMatch(node: RDFStore#Node): RDFStore#NodeMatch = PlainNode(node)

  override def diff(g1: RDFStore#Graph, g2: RDFStore#Graph): RDFStore#Graph = throw new Exception("DIFF NOT IMPLEMENTED")

  override def fromTriple(triple: RDFStore#Triple): (RDFStore#Node, RDFStore#URI, RDFStore#Node) = (triple.subject, triple.predicate, triple.objectt)

  override def makeBNode(): RDFStore#BNode = new RDFStoreBlankNode(RDFStoreW.rdf.createBlankNode())

  override def graphToIterable(graph: RDFStore#Graph): Iterable[RDFStore#Triple] = graph.triples

  override def foldNode[T](node: RDFStore#Node)(funURI: (RDFStore#URI) => T, funBNode: (RDFStore#BNode) => T, funLiteral: (RDFStore#Literal) => T): T = node.jsNode.interfaceName.asInstanceOf[js.String] match {
    case "NamedNode" => funURI(node.asInstanceOf[RDFStoreNamedNode])
    case "BlankNode" => funBNode(node.asInstanceOf[RDFStoreBlankNode])
    case "Literal" => funLiteral(node.asInstanceOf[RDFStoreLiteral])
  }

  override def makeLang(s: String): RDFStore#Lang = s

  override def makeBNodeLabel(s: String): RDFStore#BNode = new RDFStoreBlankNode(js.Dynamic.newInstance(RDFStoreW.rdf_api.BlankNode)(s))

  override def makeLangTaggedLiteral(lexicalForm: String, lang: RDFStore#Lang): RDFStore#Literal =
    new RDFStoreLiteral(js.Dynamic.newInstance(RDFStoreW.rdf_api.Literal)(lexicalForm, lang, null))

  override def fromLiteral(literal: RDFStore#Literal): (String, RDFStore#URI, Option[RDFStore#Lang]) = {
    val lexicalForm: String = literal.nominalValue.asInstanceOf[String]
    val datatype: RDFStore#URI = if (literal.datatype == null) {
      null
    } else {
      makeUri(literal.datatype)
    }
    var lang: Option[RDFStore#Lang] = if (literal.language == null) {
      None
    } else {
      Some(literal.language.asInstanceOf[RDFStore#Lang])
    }

    (lexicalForm, datatype, lang)
  }

  override def makeUri(s: String): RDFStore#URI = {
    new RDFStoreNamedNode(RDFStoreW.rdf.createNamedNode(s))
  }

  override def makeTriple(s: RDFStore#Node, p: RDFStore#URI, o: RDFStore#Node): RDFStore#Triple = {
    val sNode: js.Any = s.jsNode
    val pNode: js.Any = p.jsNode
    val oNode: js.Any = o.jsNode
    new RDFStoreTriple(RDFStoreW.rdf.createTriple(sNode, pNode, oNode))
  }

  override def ANY: RDFStore#NodeAny = JsANY

  override def makeGraph(it: Iterable[RDFStore#Triple]): RDFStore#Graph = {
    var triplesArray = js.Dynamic.newInstance(global.Array)()
    for (triple <- it) {
      triplesArray.push(triple.triple)
    }

    new RDFStoreGraph(RDFStoreW.rdf.createGraph(triplesArray))
  }

  override def fromLang(l: RDFStore#Lang): String = l

  override def foldNodeMatch[T](nodeMatch: RDFStore#NodeMatch)(funANY: => T, funNode: (RDFStore#Node) => T): T =
    nodeMatch match {
      case PlainNode(node) => funNode(node)
      case _ => funANY
    }

  // graph isomorphism ( why does this have to be created anew every time? ie. why a def? )
  def iso = new GraphIsomorphism()(new RDFStoreOps())

  override def isomorphism(left: RDFStore#Graph, right: RDFStore#Graph): Boolean =
    iso.findAnswer(left, right).isSuccess

  def graphSize(g: RDFStore#Graph): Int = g.size

  override def find(graph: RDFStore#Graph, subject: RDFStore#NodeMatch, predicate: RDFStore#NodeMatch, objectt: RDFStore#NodeMatch): Iterator[RDFStore#Triple] = {

    val subjectNode: js.Dynamic = subject match {
      case PlainNode(node) => node.jsNode
      case _ => null
    }

    val predicateNode: js.Dynamic = predicate match {
      case PlainNode(node) => node.jsNode
      case _ => null
    }

    val objectNode: js.Dynamic = objectt match {
      case PlainNode(node) => node.jsNode
      case _ => null
    }

    var filtered: js.Array[js.Dynamic] = graph.graph.applyDynamic("match")(subjectNode, predicateNode, objectNode, null).triples.asInstanceOf[js.Array[js.Dynamic]]
    var filteredList: List[RDFStore#Triple] = List[RDFStore#Triple]()
    for (triple <- filtered) {
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
    var lang: String = null
    var datatypeString: String = null

    if (lexicalForm.indexOf("@") != -1) {
      val parts = lexicalForm.split("@")
      value = parts(0)
      lang = parts(1)
    }

    if (datatype != null) {
      datatypeString = getString(datatype)
    }

    new RDFStoreLiteral(js.Dynamic.newInstance(RDFStoreW.rdf_api.Literal)(value, lang, datatypeString))
  }
}
