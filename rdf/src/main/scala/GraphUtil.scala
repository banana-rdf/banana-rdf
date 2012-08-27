package org.w3.banana

import scalaz._
import scalaz.Validation._

trait GraphUtil[Rdf <: RDF] {
  this: Diesel[Rdf] =>

  import ops._

  def getObjects(graph: Rdf#Graph, subject: Rdf#Node, predicate: Rdf#URI): Iterable[Rdf#Node] =
    find(graph, toConcreteNodeMatch(subject), toConcreteNodeMatch(predicate), ANY).map(t => fromTriple(t)._3).toIterable

  def getPredicates(graph: Rdf#Graph, subject: Rdf#Node): Iterable[Rdf#URI] =
    find(graph, toConcreteNodeMatch(subject), ANY, ANY).map(t => fromTriple(t)._2).toIterable

  def getSubjects(graph: Rdf#Graph, predicate: Rdf#URI, obj: Rdf#Node): Iterable[Rdf#Node] =
    find(graph, ANY, toConcreteNodeMatch(predicate), toConcreteNodeMatch(obj)).map(t => fromTriple(t)._1).toIterable

}
