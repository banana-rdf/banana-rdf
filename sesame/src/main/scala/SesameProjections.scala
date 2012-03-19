package org.w3.rdf.sesame

import org.w3.rdf._
import scala.collection.JavaConverters._

object SesameProjections extends Projections[Sesame] {
  import SesameOperations._
  def getObjects(graph: Graph, subject: IRI, predicate: IRI): Iterable[Node] = {
    import org.openrdf.model.util._
    new Iterable[Node] {
      def iterator = GraphUtil.getObjectIterator(graph, subject, predicate).asScala
    }
  }
}
