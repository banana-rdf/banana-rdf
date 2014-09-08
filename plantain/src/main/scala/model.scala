package org.w3.banana.plantain.model

import akka.http.model.Uri
import info.aduna.iteration.CloseableIteration
import org.openrdf.model
import org.openrdf.model.impl._
import org.openrdf.model.{ BNode => SesameBNode, Graph => SesameGraph, Literal => SesameLiteral, URI => SesameURI, _ }
import org.openrdf.query.QueryEvaluationException
import org.openrdf.query.algebra.evaluation.TripleSource

object Graph {
  val vf: ValueFactory = ValueFactoryImpl.getInstance()

  val empty = Graph(Map.empty, 0)

  implicit def toTripleSource(g: Graph): TripleSource = new TripleSource {
    override def getStatements(subject: Resource,
      predicate: model.URI,
      objectt: Value,
      contexts: Resource*): CloseableIteration[_ <: Statement, QueryEvaluationException] = {
      if (contexts.nonEmpty)
        throw new UnsupportedOperationException
      else {
        val s = if (subject == null) ANY else PlainNode(Node.fromSesame(subject))
        val p = if (predicate == null) ANY else PlainNode(Node.fromSesame(predicate))
        val o = if (objectt == null) ANY else PlainNode(Node.fromSesame(objectt))
        val it = g.find(s, p, o).iterator
        new CloseableIteration[Statement, QueryEvaluationException] {
          def close(): Unit = ()
          def hasNext(): Boolean = it.hasNext
          def next(): Statement = Triple.asSesame(it.next())
          def remove(): Unit = throw new UnsupportedOperationException
        }
      }
    }

    override def getValueFactory = vf
  }

}

case class Graph(spo: Map[Node, Map[URI, Vector[Node]]], size: Int) {

  def triples: Iterable[Triple] =
    for {
      (s, pos) <- spo
      (p, os) <- pos
      o <- os
    } yield Triple(s, p, o)

  def +(triple: Triple): Graph =
    this.+(triple.subject, triple.predicate, triple.objectt)

  def +(subject: Node, predicate: URI, objectt: Node): Graph = {
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

  @throws[java.util.NoSuchElementException]("if a triple does not exist")
  def removeExistingTriple(triple: Triple): Graph = {
    import triple.{ objectt, predicate, subject }
    val pos = spo(subject)
    val os = pos(predicate)
    if (os.size == 1) { // then it must contains only $objectt
      if (!os.contains(objectt)) throw new NoSuchElementException(s"$objectt not found")
      val newPos = pos - predicate
      if (newPos.isEmpty) // then it was actually the only spo!
        Graph(spo - subject, size - 1)
      else
        Graph(spo + (subject -> newPos), size - 1)
    } else {
      val newos = os filterNot { _ == objectt }
      if (newos.size == os.size) throw new NoSuchElementException(s"$objectt not found")
      val newPos = pos + (predicate -> newos)
      Graph(spo + (subject -> newPos), size - 1)
    }
  }

  def -(s: NodeMatch, p: NodeMatch, o: NodeMatch): Graph = {
    val matchedTriples: Iterable[Triple] = find(s, p, o)
    val newGraph = matchedTriples.foldLeft(this) { _.removeExistingTriple(_) }
    newGraph
  }

  def union(other: Graph): Graph = {
    val (firstGraph, secondGraph) =
      if (this.size > other.size)
        (this, other)
      else
        (other, this)
    secondGraph.triples.foldLeft(firstGraph) { _ + _ }
  }

  def find(subject: NodeMatch, predicate: NodeMatch, objectt: NodeMatch): Iterable[Triple] =
    (subject, predicate, objectt) match {
      case (ANY, ANY, ANY) => triples
      case (PlainNode(s), PlainNode(p @ URI(_)), PlainNode(o)) => {
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
      case (PlainNode(s), PlainNode(p @ URI(_)), ANY) => {
        val opt = for {
          pos <- spo.get(s)
          os <- pos.get(p)
        } yield {
          os map { Triple(s, p, _) }
        }
        opt getOrElse Iterable.empty
      }
      case _ => {
        // logger.warn(s"""inefficient pattern: ($subject, $predicate, $objectt)""")
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

  override def toString(): String = triples.mkString("(", " ", ")")

}

case class Triple(subject: Node, predicate: URI, objectt: Node)

object Triple {

  def fromSesame(statement: Statement): Triple =
    Triple(
      Node.fromSesame(statement.getSubject),
      URI(Uri(statement.getPredicate.toString)),
      Node.fromSesame(statement.getObject))

  def asSesame(triple: Triple): Statement = {
    import triple._
    new StatementImpl(
      Node.asSesame(subject).asInstanceOf[Resource],
      Node.asSesame(predicate).asInstanceOf[SesameURI],
      Node.asSesame(objectt))
  }

}

sealed trait Node

object Node {
  val xmls = URI(Uri("http://www.w3.org/2001/XMLSchema#string"))

  def fromSesame(value: Value): Node = value match {
    case resource: Resource => fromSesame(resource)
    case literal: SesameLiteral => fromSesame(literal)
  }

  def fromSesame(uri: SesameURI): URI = URI(Uri(uri.toString))

  def fromSesame(resource: Resource): Node = resource match {
    case uri: SesameURI => fromSesame(uri)
    case bnode: SesameBNode => BNode(bnode.getID)
  }

  def fromSesame(literal: SesameLiteral): Literal = {
    val lexicalForm = literal.getLabel
    val lang = literal.getLanguage

    val langOpt = if (lang == null || lang.isEmpty) None else Some(lang)
    val typ = Option(literal.getDatatype).map(u => URI(u.toString)) getOrElse xmls

    Literal(lexicalForm, typ, langOpt)

  }

  implicit def asSesame(node: Node): Value = node match {
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
    case Literal(lexicalForm, uri, None) => new LiteralImpl(lexicalForm, new URIImpl(uri.underlying.toString))
    case Literal(lexicalForm, dt, Some(lang)) => new LiteralImpl(lexicalForm, lang)
  }

}

case class URI(underlying: Uri) extends Node

case class BNode(label: String) extends Node

case class Literal(lexicalForm: String, datatype: URI, langOpt: Option[String]) extends Node

sealed trait NodeMatch

case class PlainNode(node: Node) extends NodeMatch

case object ANY extends NodeMatch
