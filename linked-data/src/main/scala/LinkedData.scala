package org.w3.linkeddata

import org.w3.rdf._

object LinkedData {

  /**
   * Provides a default instance for LinkedData
   * <ul>
   * <li>it comes with an internal memory-based knowledge base to cache the graphs</li>
   * <li>LD is a Future-based implementation (totally asynchronous)</li>
   * <li>timbl() waits for the value to be there</li>
   * </ul>
   */
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

/**
 * The interface to the Web of Data
 */
trait LinkedData[Rdf <: RDF] {

  /**
   * The abstraction for a value coming from the interaction with the Web of Data
   */
  type LD[A] <: LDInterface[A]

  trait LDInterface[S] {

    /**
     * don't forget to invoke timbl in order to get the best out of your LD!
     */
    def timbl(): S

    def map[A](f: S ⇒ A): LD[A]

    def flatMap[A](f: S ⇒ LD[A]): LD[A]

    def follow(predicate: Rdf#IRI)(implicit ev: S =:= Rdf#IRI): LD[Iterable[Rdf#Node]]

    def followAll(predicate: Rdf#IRI)(implicit ev: S =:= Iterable[Rdf#Node]): LD[Iterable[Rdf#Node]]

    def asURIs()(implicit ev: S =:= Iterable[Rdf#Node]): LD[Iterable[Rdf#IRI]]

  }

  def shutdown(): Unit

  def point[S](s: S): LD[S]

  def goto(iri: Rdf#IRI): LD[Rdf#IRI]

  /* pimps some common RDF types to get a smooth interaction with the LD */

  class IRIW(iri: Rdf#IRI) {
    def follow(predicate: Rdf#IRI): LD[Iterable[Rdf#Node]] = goto(iri).follow(predicate)
  }

  implicit def wrapIRI(iri: Rdf#IRI): IRIW = new IRIW(iri)

  class IRIsW(iris: Iterable[Rdf#Node]) {
    def follow(predicate: Rdf#IRI): LD[Iterable[Rdf#Node]] = {
      val irisLD = point(iris)
      irisLD.followAll(predicate)
    }
  }

  implicit def wrapIRIs(iris: Iterable[Rdf#Node]): IRIsW = new IRIsW(iris)

}

