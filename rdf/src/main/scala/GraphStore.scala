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

  def apply[Rdf <: RDF, M[_]](store: RDFStore[Rdf, M])(implicit ops: RDFOperations[Rdf], ldc: LDC[Rdf]): GraphStore[Rdf, M] = new GraphStore[Rdf, M] {

    def appendToGraph(uri: Rdf#URI, graph: Rdf#Graph): M[Unit] =
      store.execute(ldc.append(uri, ops.graphToIterable(graph)))
    
    def patchGraph(uri: Rdf#URI, delete: Iterable[TripleMatch[Rdf]], insert: Rdf#Graph): M[Unit] = sys.error("")
    
    def getGraph(uri: Rdf#URI): M[Rdf#Graph] = sys.error("")
    
    def removeGraph(uri: Rdf#URI): M[Unit] = sys.error("")

  }

}
