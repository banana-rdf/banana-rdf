package org.w3.banana.diesel

import org.w3.banana.*
import org.w3.banana.RDF.*

case class PredicatePointedRelGraph[Rdf <: RDF](
  p: rURI[Rdf],
  pointed: PointedRelGraph[Rdf]
):

  infix def --(s: Node[Rdf])(using ops: Ops[Rdf]): PointedRelGraph[Rdf] =
    import ops.given
    import pointed.{ graph as acc, pointer as o }
    val graph = acc + rTriple(s, p, o)
    PointedRelGraph(s, graph)

  infix def --(pointedSubject: PointedRelGraph[Rdf])(using ops: Ops[Rdf]): PointedRelGraph[Rdf] =
    import ops.given
    import pointed.{ graph as acc, pointer as o }
    import pointedSubject.{ graph as graphObject, pointer as s }
    //todo: check efficiency...
    val graph = acc ++ (Triple(s, p, o) +: graphObject.triples.toSeq)
    PointedRelGraph(s, graph)


