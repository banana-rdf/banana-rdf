package org.w3.banana

import scalaz.Validation

trait PGBElem[Rdf <: RDF, T] {

  def either: Either[PGBSubject[Rdf, T], PGBPredicateObject[Rdf, T]]

}

trait PGBSubject[Rdf <: RDF, T] {

  def makeSubject(t: T): Rdf#URI

  def extract(subject: Rdf#URI): Validation[BananaException, T]

}

trait PGBPredicateObject[Rdf <: RDF, T] {

  def predicate: Rdf#URI

  def binder: PointedGraphBinder[Rdf, T]

}
