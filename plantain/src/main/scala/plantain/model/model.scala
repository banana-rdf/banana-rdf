package org.w3.banana.plantain.model

import org.w3.banana.TripleMatch
import java.net.{ URI => jURI }
import org.slf4j.LoggerFactory
import java.util.{ Set => jSet, List => jList, Collection => jCollection, Iterator => jIterator }
import scala.collection.JavaConverters._
import org.openrdf.model.{ Graph => SesameGraph, Literal => SesameLiteral, BNode => SesameBNode, URI => SesameURI, _ }
import org.openrdf.model.impl._
import org.openrdf.query.algebra.evaluation.TripleSource
import info.aduna.iteration.CloseableIteration
import org.openrdf.query.QueryEvaluationException
import scala.Some

object Graph {

  val logger = LoggerFactory.getLogger(classOf[Graph])

  val empty = Graph(Map.empty, 0)

  val vf: ValueFactory = ValueFactoryImpl.getInstance()

}

case class Graph(spo: Map[Node, Map[URI, Vector[Node]]], size: Int) extends SesameGraph with TripleSource {

  import Graph.logger

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

  def removeExistingTriple(triple: Triple): Graph = {
    import triple.{ subject, predicate, objectt }
    val pos = spo(subject)
    val os = pos(predicate)
    if (os.size == 1) { // then it must contains only $objectt
      val newPos = pos - predicate
      if (newPos.isEmpty) // then it was actually the only spo!
        Graph(spo - subject, size - 1)
      else
        Graph(spo + (subject -> newPos), size - 1)
    } else {
      val newPos = pos + (predicate -> (os filterNot { _ == objectt }))
      Graph(spo + (subject -> newPos), size - 1)
    }
  }

  def -(tripleMatch: TripleMatch[org.w3.banana.plantain.Plantain]): Graph = {
    import tripleMatch.{ _1 => sMatch, _2 => pMatch, _3 => oMatch }
    val matchedTriples: Iterable[Triple] = find(sMatch, pMatch, oMatch)
    val newGraph = matchedTriples.foldLeft(this){ _.removeExistingTriple(_) }
    newGraph
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
      case (ANY, ANY, ANY) => triples
      case (PlainNode(s), PlainNode(Predicate(p)), PlainNode(o)) => {
        val opt = for {
          pos <- spo.get(s)
          os <- pos.get(p)
          if os contains o
        } yield Iterable(Triple(s, p, o))
        opt getOrElse Iterable.empty
      }
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
            case PlainNode(node) => os filter { _ == node }
          }
        } yield Triple(s, p, o)
      }
    }

  /* methods for Sesame's Graph */

  def add(statement: Statement): Boolean = throw new UnsupportedOperationException
  def addAll(statements: jCollection[_ <: Statement]): Boolean = throw new UnsupportedOperationException
  def clear(): Unit = throw new UnsupportedOperationException
  def contains(o: Any): Boolean = ???
  def containsAll(coll: java.util.Collection[_]): Boolean = ???
  def isEmpty(): Boolean = size == 0
  def iterator(): jIterator[Statement] = triples.iterator.map(_.asSesame).asJava
  def remove(o: Any): Boolean = throw new UnsupportedOperationException
  def removeAll(coll: jCollection[_]): Boolean = throw new UnsupportedOperationException
  def retainAll(x$1: java.util.Collection[_]): Boolean = throw new UnsupportedOperationException
  def toArray[T](a: Array[T with Object]): Array[T with Object] = ???
  def toArray(): Array[Object] = ???
  
  def add(subject: Resource, predicate: SesameURI, objectt: Value, contexts: Resource*): Boolean =
    throw new UnsupportedOperationException
  def getValueFactory(): ValueFactory = Graph.vf
  def `match`(subject: Resource, predicate: SesameURI, objectt: Value, contexts: Resource*): jIterator[Statement] = {
    if (contexts.nonEmpty)
      throw new UnsupportedOperationException
    else {
      val s = if (subject == null) ANY else PlainNode(Node.fromSesame(subject))
      val p = if (predicate == null) ANY else PlainNode(Node.fromSesame(predicate))
      val o = if (objectt == null) ANY else PlainNode(Node.fromSesame(objectt))
      find(s, p, o).iterator.map(_.asSesame).asJava
    }
  }

  /* TripleSource */

  def getStatements(subject: Resource, predicate: SesameURI, objectt: Value, contexts: Resource*): CloseableIteration[_ <: Statement, QueryEvaluationException] = {
    if (contexts.nonEmpty)
      throw new UnsupportedOperationException
    else {
      val s = if (subject == null) ANY else PlainNode(Node.fromSesame(subject))
      val p = if (predicate == null) ANY else PlainNode(Node.fromSesame(predicate))
      val o = if (objectt == null) ANY else PlainNode(Node.fromSesame(objectt))
      val it = find(s, p, o).iterator
      new CloseableIteration[Statement, QueryEvaluationException] {
        def close(): Unit = ()
        def hasNext(): Boolean = it.hasNext
        def next(): Statement = it.next().asSesame
        def remove(): Unit = throw new UnsupportedOperationException
      }
    }
  }

  override def toString(): String = {
     triples.foldRight("("){ case (triple,s) => s+" "+triple.toString }+")"
  }

}

object Triple {

  def fromSesame(statement: Statement): Triple =
    Triple(
      Node.fromSesame(statement.getSubject),
      URI.fromString(statement.getPredicate.toString),
      Node.fromSesame(statement.getObject))

}

case class Triple(subject: Node, predicate: URI, objectt: Node) {

  override def toString: String = asSesame.toString

  def asSesame: Statement =
    new StatementImpl(
      subject.asSesame.asInstanceOf[Resource],
      predicate.asSesame.asInstanceOf[SesameURI],
      objectt.asSesame)

}

object Node {

  def fromSesame(value: Value): Node = value match {
    case resource: Resource => fromSesame(resource)
    case literal: SesameLiteral => fromSesame(literal)
  }

  def fromSesame(uri: SesameURI): URI = URI.fromString(uri.toString)

  def fromSesame(resource: Resource): Node = resource match {
    case uri: SesameURI => fromSesame(uri)
    case bnode: SesameBNode => BNode(bnode.getID)
  }

  def fromSesame(literal: SesameLiteral): Literal = {
    val lexicalForm = literal.getLabel
    val lang = literal.getLanguage
    if (lang == null || lang.isEmpty) {
      val typ = URI.fromString(Option(literal.getDatatype).map(_.toString) getOrElse "http://www.w3.org/2001/XMLSchema#string")
      TypedLiteral(lexicalForm, typ)
    } else {
      LangLiteral(lexicalForm, lang)
    }
  }

}

sealed trait Node {

  override def toString: String = asSesame.toString

  def asSesame: Value = this match {
    case URI(underlying) => {
      val uriS = underlying.toString
      try {
        new URIImpl(uriS)
      } catch {
        case iae: IllegalArgumentException =>
          new SesameURI {
            override def equals(o: Any): Boolean = o.isInstanceOf[SesameURI] && o.asInstanceOf[SesameURI].toString == uriS
            def getLocalName: String = uriS
            def getNamespace: String = ""
            override def hashCode: Int = uriS.hashCode
            override def toString: String = uriS
            def stringValue: String = uriS
          }
      }
    }
    case BNode(label) => new BNodeImpl(label)
    case TypedLiteral(lexicalForm, uri) => new LiteralImpl(lexicalForm, new URIImpl(uri.underlying.toString))
    case LangLiteral(lexicalForm, lang) => new LiteralImpl(lexicalForm, lang)
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


}

sealed trait NodeMatch

case class PlainNode(node: Node) extends NodeMatch {

  override def toString: String = node.toString

}

case object ANY extends NodeMatch
