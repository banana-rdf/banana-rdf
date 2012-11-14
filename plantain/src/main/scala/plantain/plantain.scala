package org.w3.banana.plantain

import java.net.{ URI => jURI }
import java.util.UUID
import org.slf4j.{ Logger, LoggerFactory }
import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Triple => JenaTriple, Node => JenaNode, _ }
import com.hp.hpl.jena.rdf.model.AnonId
import com.hp.hpl.jena.datatypes.TypeMapper
import com.hp.hpl.jena.graph.impl.{ GraphMatcher, GraphBase }
import com.hp.hpl.jena.shared.PrefixMapping
import com.hp.hpl.jena.graph.query.{ QueryHandler, SimpleQueryHandler }
import com.hp.hpl.jena.util.iterator.{ ExtendedIterator, Filter, Map1 }
import java.util.{ Set => jSet, List => jList}
import scala.collection.JavaConverters._

object Graph {

  val logger = LoggerFactory.getLogger(classOf[Graph])

  val empty = Graph(Map.empty, 0)

  class AsExtendedIterator[T](iterator: Iterator[T]) extends ExtendedIterator[T] {
    def close(): Unit = ()
    def andThen[X <: T](other: java.util.Iterator[X]): ExtendedIterator[T] =
      new AsExtendedIterator(iterator ++ other.asScala)
    def filterDrop(f: Filter[T]): ExtendedIterator[T] = new AsExtendedIterator(iterator filterNot f.accept)
    def filterKeep(f: Filter[T]): ExtendedIterator[T] = new AsExtendedIterator(iterator filter f.accept)
    def mapWith[U](map1: Map1[T,U]): ExtendedIterator[U] = new AsExtendedIterator(iterator map map1.map1)
    def removeNext(): T = throw new UnsupportedOperationException
    def toList(): jList[T] = iterator.toList.asJava
    def toSet(): jSet[T] = iterator.toSet.asJava
    def hasNext(): Boolean = iterator.hasNext
    def next(): T = iterator.next
    def remove(): Unit = throw new UnsupportedOperationException
  }

  val Capabilities = new Capabilities {
    def addAllowed(b: Boolean): Boolean = false
    def addAllowed(): Boolean = false
    def canBeEmpty(): Boolean = true
    def deleteAllowed(b: Boolean): Boolean = false
    def deleteAllowed(): Boolean = false
    def findContractSafe(): Boolean = true
    def handlesLiteralTyping(): Boolean = true
    def iteratorRemoveAllowed(): Boolean = false
    def sizeAccurate(): Boolean = true
  }

}

case class Graph(spo: Map[Node, Map[URI, Vector[Node]]], size: Int) extends JenaGraph {

  import Graph.{ logger }

  def triples: Iterable[Triple] =
    for {
      (s, pos) <- spo
      (p, os) <- pos
      o <- os
    } yield Triple(s, p, o)


  def +(triple: Triple): Graph = {
    import triple.{ subject, predicate, objectt }
    spo.get(subject) match {
      case None => Graph(spo + (subject -> Map(predicate -> Vector(objectt))), size + 1)
      case Some(pos) => pos.get(predicate) match {
        case None => {
          val pos2 = pos + (predicate -> Vector(objectt))
          Graph(spo + (subject -> pos2), size + 1)
        }
        case Some(os) => {
          if (os contains objectt)
            this
          else {
            val pos2 = pos + (predicate -> (os :+ objectt))
            Graph(spo + (subject -> pos2), size + 1)
          }
        }
      }
    }
  }

  def union(other: Graph): Graph = {
    val (firstGraph, secondGraph) =
      if (this.size > other.size)
        (this, other)
      else
        (other, this)
    secondGraph.triples.foldLeft(firstGraph){ _ + _ }
  }

  def find(subject: NodeMatch, predicate: NodeMatch, objectt: NodeMatch): Iterable[Triple] =
    (subject, predicate, objectt) match {
      case (PlainNode(s), ANY, ANY) =>
        for {
          (p, os) <- spo.get(s) getOrElse Iterable.empty
          o <- os
        } yield Triple(s, p, o)
      case (PlainNode(s), PlainNode(Predicate(p)), ANY) => {
        val opt = for {
          pos <- spo.get(s)
          os <- pos.get(p)
        } yield {
          os map { Triple(s, p, _) }
        }
        opt getOrElse Iterable.empty
      }
      case _ => {
        logger.warn(s"""inefficient pattern: ($subject, $predicate, $objectt)""")
        for {
          (s, pos) <- subject match {
            case ANY => spo
            case PlainNode(node) => spo filterKeys { _ == node }
          }
          (p, os) <- predicate match {
            case ANY => pos
            case PlainNode(node) => pos filterKeys { _ == node }
          }
          o <- objectt match {
            case ANY => os
            case PlainNode(node) => if (os contains node) os else Iterable.empty
          }
        } yield Triple(s, p, o)
      }
    }

  /* methods for Jena's Graph */

  def close(): Unit = throw new UnsupportedOperationException
  def dependsOn(other: JenaGraph): Boolean = throw new UnsupportedOperationException
  def getBulkUpdateHandler(): BulkUpdateHandler = throw new UnsupportedOperationException
  def getCapabilities(): Capabilities = Graph.Capabilities
  def getEventManager(): GraphEventManager = throw new UnsupportedOperationException
  def getPrefixMapping(): PrefixMapping = throw new UnsupportedOperationException
  def getReifier(): Reifier = throw new UnsupportedOperationException
  def getStatisticsHandler(): GraphStatisticsHandler = throw new UnsupportedOperationException
  def getTransactionHandler(): TransactionHandler = throw new UnsupportedOperationException
  def isClosed(): Boolean = true
  def queryHandler(): QueryHandler = new SimpleQueryHandler(this)
  def add(triple: JenaTriple): Unit = throw new UnsupportedOperationException
  def isEmpty(): Boolean = size == 0
  def contains(triple: JenaTriple): Boolean =
    this.contains(triple.getSubject, triple.getPredicate, triple.getObject)
  def contains(subject: JenaNode, predicate: JenaNode, objectt: JenaNode): Boolean = {
    val s = NodeMatch.fromJena(subject)
    val p = NodeMatch.fromJena(predicate)
    val o = NodeMatch.fromJena(objectt)
    find(s, p, o).nonEmpty // yes we can do better :-)
  }
   def delete(triple: JenaTriple): Unit = throw new UnsupportedOperationException
   def find(subject: JenaNode, predicate: JenaNode, objectt: JenaNode): ExtendedIterator[JenaTriple] = {
     val s = NodeMatch.fromJena(subject)
     val p = NodeMatch.fromJena(predicate)
     val o = NodeMatch.fromJena(objectt)
     val iterable = find(s, p, o) map (_.asJena)
     new Graph.AsExtendedIterator(iterable.iterator)
   }
   def find(triple: TripleMatch): ExtendedIterator[JenaTriple] =
     this.find(triple.getMatchSubject, triple.getMatchPredicate, triple.getMatchObject)
   def isIsomorphicWith(other: JenaGraph): Boolean = GraphMatcher.equals(this, other)

}

case class Triple(subject: Node, predicate: URI, objectt: Node) {

  override def toString: String = asJena.toString

  def asJena: JenaTriple = JenaTriple.create(subject.asJena, predicate.asJena, objectt.asJena)

}

object Node {

  lazy val mapper = TypeMapper.getInstance

  def fromJena(node: JenaNode): Node = node match {
    case iri: Node_URI => URI(new jURI(iri.getURI))
    case bnode: Node_Blank => BNode(bnode.getBlankNodeId.getLabelString)
    case tl: Node_Literal if tl.getLiteralLanguage == null || tl.getLiteralLanguage.isEmpty => {
      val lexicalForm = tl.getLiteralLexicalForm.toString
      val uri =
        Option(tl.getLiteralDatatype).map(typ => URI.fromString(typ.getURI)) getOrElse URI.fromString(("http://www.w3.org/2001/XMLSchema#string"))
      TypedLiteral(lexicalForm, uri)
    }
    case ll: Node_Literal => {
      val lexicalForm = ll.getLiteralLexicalForm.toString
      val lang = ll.getLiteralLanguage
      LangLiteral(lexicalForm, lang)
    }
  }

}

sealed trait Node {

  override def toString: String = asJena.toString

  def asJena: JenaNode = this match {
    case URI(underlying) => JenaNode.createURI(underlying.toString)
    case BNode(label) => JenaNode.createAnon(AnonId.create(label))
    case TypedLiteral(lexicalForm, uri) => JenaNode.createLiteral(lexicalForm, null, Node.mapper.getTypeByName(uri.toString))
    case LangLiteral(lexicalForm, lang) => JenaNode.createLiteral(lexicalForm, lang, null)
  }

}

case class URI(underlying: jURI) extends Node

object URI {

  def fromString(s: String): URI = URI(new jURI(s))

}

case class BNode(label: String) extends Node

sealed trait Literal extends Node {
  def lexicalForm: String
}

case class TypedLiteral(lexicalForm: String, uri: URI) extends Literal

case class LangLiteral(lexicalForm: String, lang: String) extends Literal

object Predicate {

  def unapply(node: Node): Option[URI] = node match {
    case uri@URI(_) => Some(uri)
    case _ => None
  }

}



// types for the graph traversal API

object NodeMatch {

  def fromJena(node: JenaNode): NodeMatch = node match {
    case null | JenaNode.ANY => ANY
    case concrete: Node_Concrete => PlainNode(Node.fromJena(concrete))
  }

}

sealed trait NodeMatch

case class PlainNode(node: Node) extends NodeMatch {

  override def toString: String = node.toString

}

case object ANY extends NodeMatch
