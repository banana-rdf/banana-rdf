package org.w3.banana.bigdata

import org.w3.banana.RDFOps
import org.w3.banana.bigdata.Bigdata

import scalaz.Value

/**
 * As there is not BigdataModel I decided to created my own, by modifying plantain graph
 * @param spo
 * @param size
 */
case class BigdataGraph(spo: Map[Bigdata#Node, Map[Bigdata#URI, Vector[Bigdata#Node]]], size: Int) {

  private lazy val ops = implicitly[RDFOps[Bigdata]] //temporal


    def triples: Iterable[Bigdata#Triple] =
      for {
        (s, pos) <- spo
        (p, os) <- pos
        o <- os
      } yield ops.makeTriple(s, p, o)

    def +(triple: Bigdata#Triple): BigdataGraph =
      this.+(triple.getSubject, triple.getPredicate, triple.getObject)

    def +(subject: Bigdata#Node, predicate: Bigdata#URI, objectt: Bigdata#Node): BigdataGraph = {
      spo.get(subject) match {
        case None => BigdataGraph(spo + (subject -> Map(predicate -> Vector(objectt))), size + 1)
        case Some(pos) => pos.get(predicate) match {
          case None =>
            val pos2 = pos + (predicate -> Vector(objectt))
            BigdataGraph(spo + (subject -> pos2), size + 1)
          case Some(os) =>
            if (os contains objectt)
              this
            else {
              val pos2 = pos + (predicate -> (os :+ objectt))
              BigdataGraph(spo + (subject -> pos2), size + 1)
            }
        }
      }
    }

  def -(triple: Bigdata#Triple): BigdataGraph = removeExistingTriple(triple)

    @throws[java.util.NoSuchElementException]("if a triple does not exist")
  def removeExistingTriple(triple: Bigdata#Triple): BigdataGraph = {
    val (subject, predicate, objectt) = (triple.getSubject,triple.getPredicate,triple.getObject)
    val pos = spo(subject)
    val os = pos(predicate)
    if (os.size == 1) { // then it must contains only $objectt
      if (!os.contains(objectt)) throw new NoSuchElementException(s"$objectt not found")
      val newPos = pos - predicate
      if (newPos.isEmpty) // then it was actually the only spo!
        BigdataGraph(spo - subject, size - 1)
      else
        BigdataGraph(spo + (subject -> newPos), size - 1)
    } else {
      val newos = os filterNot { _ == objectt }
      if (newos.size == os.size) throw new NoSuchElementException(s"$objectt not found")
      val newPos = pos + (predicate -> newos)
      BigdataGraph(spo + (subject -> newPos), size - 1)
    }
  }
/*
  def -(s: Bigdata#NodeMatch, p: Bigdata#NodeMatch, o: Bigdata#NodeMatch): BigdataGraph = {
    val matchedTriples: Iterable[Bigdata#Triple] = find(s, p, o)
    val newGraph = matchedTriples.foldLeft(this) { _.removeExistingTriple(_) }
    newGraph
  }*/

  def union(other: BigdataGraph): BigdataGraph = {
    val (firstGraph, secondGraph) =
      if (this.size > other.size)
        (this, other)
      else
        (other, this)
    secondGraph.triples.foldLeft(firstGraph) { _ + _ }
  }

  def find(subject: Bigdata#NodeMatch, predicate: Bigdata#NodeMatch, objectt: Bigdata#NodeMatch): Iterable[Bigdata#Triple] =
    (subject, predicate, objectt) match {
      case (null, null, null) => triples

      case (s:Bigdata#Node, null, null) =>
        for {
          (p, os) <- spo.getOrElse(s, Iterable.empty)
          o <- os
        } yield ops.makeTriple(s, p, o)
      case (s:Bigdata#Node, p:Bigdata#URI, null) =>
        val opt = for {
          pos <- spo.get(s)
          os <- pos.get(p)
        } yield {
          os map { ops.makeTriple(s, p, _) }
        }
        opt getOrElse Iterable.empty

      case (s:Bigdata#Node, p:Bigdata#URI, o:Bigdata#Node) =>
        val opt = for {
          pos <- spo.get(s)
          os <- pos.get(p)
          if os contains o
        } yield Iterable(ops.makeTriple(s, p, o))
        opt getOrElse Iterable.empty

      case _ =>
        // logger.warn(s"""inefficient pattern: ($subject, $predicate, $objectt)""")
        for {
          (s, pos) <- subject match {
            case null => spo
            case node => spo filterKeys { _ == node }
          }
          (p, os) <- predicate match {
            case null => pos
            case node => pos filterKeys { _ == node }
          }
          o <- objectt match {
            case null => os
            case node => if (os contains node) os else Iterable.empty
          }
        } yield ops.makeTriple(s, p, o)
    }



  override def toString: String = triples.mkString("(", " ", ")")

}
