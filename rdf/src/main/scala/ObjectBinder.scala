package org.w3.banana

import scalaz.{ Validation, Success, Failure }

case class Property[Rdf <: RDF, T](uri: Rdf#URI, binder: PointedGraphBinder[Rdf, T])

trait ComplexBinder[Rdf <: RDF, T] extends PointedGraphBinder[Rdf, T] {
  
  def instanceOf(clazz: Rdf#URI): ComplexBinder[Rdf, T]

  def uriBinder(binder: URIBinder[Rdf, T]): ComplexBinder[Rdf, T]

}




trait SealedBinder[Rdf <: RDF, T] extends PointedGraphBinder[Rdf, T]

trait ObjectBinderDSL[Rdf <: RDF] {
  self: Diesel[Rdf] =>

  def property[T](uri: Rdf#URI)(implicit binder: PointedGraphBinder[Rdf, T]): Property[Rdf, T] =
    Property[Rdf, T](uri, binder)

  case class PropertiesBuilder1[T1] private[banana] (p1: Property[Rdf, T1]) {

    def bind[T](apply: T1 => T, unapply: T => Option[T1]): ComplexBinderBase[T] = {

      val fromPG: PointedGraph[Rdf] => Validation[BananaException, T] =
        (pointed: PointedGraph[Rdf]) => {
          for {
            t1 <- (pointed / p1.uri).as[T1](p1.binder)
          } yield {
            apply(t1)
          }
        }

      val toPG: (Rdf#Node, T) => PointedGraph[Rdf] =
        (subject: Rdf#Node, t: T) => {
          val t1 = unapply(t).get
          subject.--(p1.uri).->-(t1)(p1.binder)
        }

      ComplexBinderBase(fromPG, toPG)

    }

    def property[T2](p2: Property[Rdf, T2]): PropertiesBuilder2[T1, T2] = PropertiesBuilder2(p1, p2)

  }

  case class PropertiesBuilder2[T1, T2] private[banana] (t1: Property[Rdf, T1], t2: Property[Rdf, T2]) {

    def bind[T](apply: (T1, T2) => T, unapply: T => Option[(T1, T2)]): ComplexBinderBase[T] = null

//    def property[T3](p: Property[T3]): PropertiesBuilder2[T1, T2, T3] = PropertiesBuilder3(t1, t2, t3)

  }

  object ObjectBinder {

    def property[T](p: Property[Rdf, T]): PropertiesBuilder1[T] = PropertiesBuilder1[T](p)

    def constant[T](constObj: T, constUri: Rdf#URI): PointedGraphBinder[Rdf, T] = NodeToPointedGraphBinder(UriToNodeBinder( new URIBinder[Rdf, T] {

      def fromUri(uri: Rdf#URI): Validation[BananaException, T] =
        if (constUri == uri)
          Success(constObj)
        else
          Failure(WrongExpectation("was expecting the constant URI " + constUri + " but got " + uri))
  
      def toUri(t: T): Rdf#URI = constUri

    }))

  }

  case class ComplexBinderBase[T] private[banana] (
    // we don't expect this fromPointedGraph to check the subject and the classes
    // it operated only on the properties and their objects
    fromPG: PointedGraph[Rdf] => Validation[BananaException, T],
    // toPointedGraph needs to know what kind of subject we are dealing with
    // but there was already enough information to build a PointedGraph otherwise
    toPG: (Rdf#Node, T) => PointedGraph[Rdf],
    // handles the classes that we want to associate with this ComplexBinder
    classes: List[Rdf#URI] = List.empty,
    // says optionaly how to build a URI binder
    uriBinderOpt: Option[URIBinder[Rdf, T]] = None) extends ComplexBinder[Rdf, T] {
    
    def instanceOf(clazz: Rdf#URI): ComplexBinder[Rdf, T] = this.copy(classes = clazz :: classes)
  
    def uriBinder(binder: URIBinder[Rdf, T]): ComplexBinder[Rdf, T] = this.copy(uriBinderOpt = Some(binder))
  
    def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
      if (classes forall { clazz => pointed.isA(clazz) }) {
        fromPG(pointed)
      } else {
        val c = classes.map(_.toString).mkString("{ ", ", ", " }")
        val msg = pointed.node + " is not a inhabitant of all these classes: " + c
        Failure(WrongExpectation(msg))
      }
    }
  
    def toPointedGraph(t: T): PointedGraph[Rdf] = {
      val subject: Rdf#Node = uriBinderOpt match {
        case None => bnode()
        case Some(uriBinder) => uriBinder.toUri(t)
      }
      var pointed = toPG(subject, t)
      classes foreach { clazz => pointed = pointed.a(clazz) }
      pointed
    }
  
  
  }
  
  
}
