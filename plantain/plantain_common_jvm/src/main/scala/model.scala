package org.w3.banana.plantain.model

case class Graph[U](spo: Map[Node, Map[URI[U], Vector[Node]]], size: Int) {

  def triples: Iterable[Triple[U]] =
    for {
      (s, pos) <- spo
      (p, os) <- pos
      o <- os
    } yield Triple(s, p, o)

  def +(triple: Triple[U]): Graph[U] =
    this.+(triple.subject, triple.predicate, triple.objectt)

  def +(subject: Node, predicate: URI[U], objectt: Node): Graph[U] = {
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
  def removeExistingTriple(triple: Triple[U]): Graph[U] = {
    import triple.{ objectt, predicate, subject }
    val pos = spo(subject)
    val os = pos(predicate)
    if (os.size == 1) { // then it must contains only $objectt
      if (!os.contains(objectt)) throw new NoSuchElementException(s"$objectt not found")
      val newPos = pos - predicate
      if (newPos.isEmpty) // then it was actually the only spo!
        Graph[U](spo - subject, size - 1)
      else
        Graph[U](spo + (subject -> newPos), size - 1)
    } else {
      val newos = os filterNot { _ == objectt }
      if (newos.size == os.size) throw new NoSuchElementException(s"$objectt not found")
      val newPos = pos + (predicate -> newos)
      Graph[U](spo + (subject -> newPos), size - 1)
    }
  }

  def -(s: NodeMatch, p: NodeMatch, o: NodeMatch): Graph[U] = {
    val matchedTriples: Iterable[Triple[U]] = find(s, p, o)
    val newGraph = matchedTriples.foldLeft(this) { _.removeExistingTriple(_) }
    newGraph
  }

  def union(other: Graph[U]): Graph[U] = {
    val (firstGraph, secondGraph) =
      if (this.size > other.size)
        (this, other)
      else
        (other, this)
    secondGraph.triples.foldLeft(firstGraph) { _ + _ }
  }

  def find(subject: NodeMatch, predicate: NodeMatch, objectt: NodeMatch): Iterable[Triple[U]] =
    (subject, predicate, objectt) match {
      case (ANY, ANY, ANY) => triples
      case (PlainNode(s), PlainNode(p: URI[U]), PlainNode(o)) => {
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
      case (PlainNode(s), PlainNode(p: URI[U]), ANY) => {
        val opt = for {
          pos <- spo.get(s)
          os <- pos.get(p)
        } yield {
          os map { Triple[U](s, p, _) }
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

case class Triple[U](subject: Node, predicate: URI[U], objectt: Node)

sealed trait Node

case class URI[U](underlying: U) extends Node

case class BNode(label: String) extends Node

case class Literal[U](lexicalForm: String, datatype: URI[U], langOpt: Option[String]) extends Node

sealed trait NodeMatch

case class PlainNode(node: Node) extends NodeMatch

case object ANY extends NodeMatch
