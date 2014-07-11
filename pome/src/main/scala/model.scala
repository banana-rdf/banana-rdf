package org.w3.banana.pome.model

import akka.http.model.{IllegalUriException, Uri}

object Graph {

  val empty = Graph(Map.empty, 0)

}

case class Graph(spo: Map[Node, Map[LazyURI, Vector[Node]]], size: Int) {

  def triples: Iterable[Triple] =
    for {
      (s, pos) <- spo
      (p, os) <- pos
      o <- os
    } yield Triple(s, p, o)

  def +(triple: Triple): Graph =
    this.+(triple.subject, triple.predicate, triple.objectt)

  

  def +(subject: Node, predicate: LazyURI, objectt: Node): Graph = {
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
    import triple.{objectt, predicate, subject}
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

  def -(s: NodeMatch, p: NodeMatch, o: NodeMatch): Graph = {
    val matchedTriples: Iterable[Triple] = find(s, p, o)
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
      case (PlainNode(s), PlainNode(p@LazyURI(_)), PlainNode(o)) => {
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
      case (PlainNode(s), PlainNode(p@LazyURI(_)), ANY) => {
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

case class Triple(subject: Node, predicate: LazyURI, objectt: Node)

sealed trait Node

/**
 * A lazy Uri that parses it only when needed
 * Use case: parsing Uris in JS is slow, so it is better to do it only when required.
 * Ideally one would request zipped, canonicalized n-triples and never parse the uris at all.
 */
case class LazyURI(string: String) extends Node {
  def this(uri: Uri) = {
    this(uri.toString())
    this.uri = uri
  }
  private var uri: Uri = null

  @throws[IllegalUriException]("problem parsing uri")
  def parsed: Uri = {
    if (uri==null) uri = Uri(string)
    uri
  }
}


//
//object LazyURI {
//  def apply(string: String): LazyURI = new UnparsedUri(string)
//  def apply(uri: Uri): LazyURI = new ParsedUri(uri)
//  def unapply(uri: LazyURI): Option[String] = uri match {
//    case u: UnparsedUri => Some(u.string)
//    case p: ParsedUri => Some(p.string)
//  }
//}
//
//class UnparsedUri(val string: String) extends LazyURI {
//  override lazy val parsed = Uri(string)
//
//  override def hashCode() = string.hashCode()
//
//  override def equals(obj: scala.Any) = super.equals(obj)
//}
//
//class ParsedUri(val parsed: Uri) extends LazyURI {
//  override lazy val string = parsed.toString
//
//  override def hashCode() = string.hashCode()
//
//  override def equals(obj: scala.Any) = super.equals(obj)
//}

case class BNode(label: String) extends Node

case class Literal(lexicalForm: String, datatype: LazyURI, langOpt: Option[String]) extends Node

sealed trait NodeMatch

case class PlainNode(node: Node) extends NodeMatch

case object ANY extends NodeMatch
