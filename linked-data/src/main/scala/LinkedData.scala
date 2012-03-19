package org.w3.linkeddata

import org.w3.rdf._

object LinkedData {

  def inMemoryImpl[Rdf <: RDF](
    ops: RDFOperations[Rdf],
    projections: Projections[Rdf],
    utils: RDFUtils[Rdf],
    turtleReader: RDFReader[Rdf, Turtle],
    turtleWriter: TurtleWriter[Rdf]): LinkedData[Rdf] =
    new LinkedDataMemoryKB(
      ops,
      projections,
      utils,
      turtleReader,
      turtleWriter)

}

trait LinkedData[Rdf <: RDF] {

  type LD[A] <: LDInterface[A]

  trait LDInterface[S] {

    def timbl(): S

    def map[A](f: S ⇒ A): LD[A]

    def flatMap[A](f: S ⇒ LD[A]): LD[A]

    def follow(predicate: Rdf#IRI)(implicit ev: S =:= Rdf#IRI): LD[Iterable[Rdf#Node]]

    def followAll(predicate: Rdf#IRI)(implicit ev: S =:= Iterable[Rdf#IRI]): LD[Iterable[Rdf#Node]]

    def asURIs()(implicit ev: S =:= Iterable[Rdf#Node]): LD[Iterable[Rdf#IRI]]

  }

  def shutdown(): Unit

  def point[S](s: S): LD[S]

  def goto(iri: Rdf#IRI): LD[Rdf#IRI]

  /* pimps */

  class IRIW(iri: Rdf#IRI) {
    def follow(predicate: Rdf#IRI): LD[Iterable[Rdf#Node]] = goto(iri).follow(predicate)
  }

  implicit def wrapIRI(iri: Rdf#IRI): IRIW = new IRIW(iri)

  class IRIsW(iris: Iterable[Rdf#IRI]) {
    def follow(predicate: Rdf#IRI): LD[Iterable[Rdf#Node]] = {
      val irisLD = point(iris)
      irisLD.followAll(predicate)
    }
  }

  implicit def wrapIRIs(iris: Iterable[Rdf#IRI]): IRIsW = new IRIsW(iris)

}

