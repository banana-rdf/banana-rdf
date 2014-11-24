package org.w3.banana.plantain.model

object Graph {

  def empty[S, P, O]: Graph[S, P, O] = Graph(Map.empty, 0)

}

case class Graph[S, P, O](spo: Map[S, Map[P, Vector[O]]], size: Int) {

  def triples: Iterable[(S, P, O)] =
    for {
      (s, pos) <- spo
      (p, os) <- pos
      o <- os
    } yield (s, p, o)

  def +(subject: S, predicate: P, objectt: O): Graph[S, P, O] = {
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
  def -(subject: S, predicate: P, objectt: O): Graph[S, P, O] = {
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
      val newos = os.filterNot { _ == objectt }
      if (newos.size == os.size) throw new NoSuchElementException(s"$objectt not found")
      val newPos = pos + (predicate -> newos)
      Graph(spo + (subject -> newPos), size - 1)
    }
  }

  def -(s: Option[S], p: Option[P], o: Option[O]): Graph[S, P, O] = {
    val matchedTriples: Iterable[(S, P, O)] = find(s, p, o)
    matchedTriples.foldLeft(this) { case (graph, (s, p, o)) => graph - (s, p, o) }
  }

  def union(other: Graph[S, P, O]): Graph[S, P, O] = {
    val (firstGraph, secondGraph) =
      if (this.size > other.size) (this, other)
      else (other, this)
    secondGraph.triples.foldLeft(firstGraph) { case (graph, (s, p, o)) => graph + (s, p, o) }
  }

  def find(subject: Option[S], predicate: Option[P], objectt: Option[O]): Iterable[(S, P, O)] =
    (subject, predicate, objectt) match {

      case (None, None, None) => triples

      case (Some(s), Some(p), Some(o)) =>
        val opt = for {
          pos <- spo.get(s)
          os <- pos.get(p)
          if os contains o
        } yield Iterable((s, p, o))
        opt getOrElse Iterable.empty

      case (Some(s), None, None) =>
        for {
          (p, os) <- spo.get(s) getOrElse Iterable.empty
          o <- os
        } yield (s, p, o)

      case (Some(s), Some(p), None) =>
        val opt = for {
          pos <- spo.get(s)
          os <- pos.get(p)
        } yield {
          os.map { (s, p, _) }
        }
        opt getOrElse Iterable.empty

      case _ =>
        // logger.warn(s"""inefficient pattern: ($subject, $predicate, $objectt)""")
        for {
          (s, pos) <- subject match {
            case None       => spo
            case Some(node) => spo.filterKeys { _ == node }
          }
          (p, os) <- predicate match {
            case None       => pos
            case Some(node) => pos filterKeys { _ == node }
          }
          o <- objectt match {
            case None => os
            case Some(node) => if (os contains node) os else Iterable.empty
          }
        } yield (s, p, o)

    }

  override def toString(): String = triples.mkString("(", " ", ")")

}

