package org.w3.rdf.sesame

import org.w3.rdf._
import scala.collection.JavaConverters._

object SesameProjections extends RDFProjections[Sesame] {

  import SesameOperations._

  def getObjects(graph: Sesame#Graph, subject: Sesame#Node, predicate: Sesame#IRI): Iterable[Sesame#Node] = {

    import org.openrdf.model.util._

    def iterable(subject: org.openrdf.model.Resource) = new Iterable[Sesame#Node] {
      def iterator = GraphUtil.getObjectIterator(graph, subject, predicate).asScala
    }
    
    Node.fold(subject)(
      iri => iterable(iri),
      bnode => iterable(bnode),
      lit => Seq.empty
    )

  }

}
