package org.w3.banana

/**
 * to manipulate named graphs
 */
trait GraphStore[Rdf <: RDF, M[_]] {

  def appendToGraph(uri: Rdf#URI, graph: Rdf#Graph): M[Unit]

  def patchGraph(uri: Rdf#URI, delete: Iterable[TripleMatch[Rdf]], insert: Rdf#Graph): M[Unit]

  def getGraph(uri: Rdf#URI): M[Rdf#Graph]

  def removeGraph(uri: Rdf#URI): M[Unit]

}

object GraphStore {

  def apply[Rdf <: RDF, M[_]](store: RDFStore[Rdf, M])(implicit ops: RDFOps[Rdf]): GraphStore[Rdf, M] = new GraphStore[Rdf, M] {

    def appendToGraph(uri: Rdf#URI, graph: Rdf#Graph): M[Unit] =
      store.execute(Command.append(uri, ops.graphToIterable(graph)))

    def patchGraph(uri: Rdf#URI, delete: Iterable[TripleMatch[Rdf]], insert: Rdf#Graph): M[Unit] =
      store.execute(Command.patch(uri, delete, ops.graphToIterable(insert)))

    def getGraph(uri: Rdf#URI): M[Rdf#Graph] =
      store.execute(Command.get(uri))

    def removeGraph(uri: Rdf#URI): M[Unit] =
      store.execute(Command.delete(uri))

  }

}
