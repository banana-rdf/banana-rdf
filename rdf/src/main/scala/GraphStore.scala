package org.w3.banana

import scala.concurrent.Future

/**
 * to manipulate named graphs
 */
trait GraphStore[Rdf <: RDF] {

  def appendToGraph(uri: Rdf#URI, graph: Rdf#Graph): Future[Unit]

  def patchGraph(uri: Rdf#URI, delete: Iterable[TripleMatch[Rdf]], insert: Rdf#Graph): Future[Unit]

  def getGraph(uri: Rdf#URI): Future[Rdf#Graph]

  def removeGraph(uri: Rdf#URI): Future[Unit]

}

object GraphStore {

  def apply[Rdf <: RDF](store: RDFStore[Rdf])(implicit ops: RDFOps[Rdf]): GraphStore[Rdf] = new GraphStore[Rdf] {

    def appendToGraph(uri: Rdf#URI, graph: Rdf#Graph): Future[Unit] =
      store.execute(Command.append(uri, ops.graphToIterable(graph)))

    def patchGraph(uri: Rdf#URI, delete: Iterable[TripleMatch[Rdf]], insert: Rdf#Graph): Future[Unit] =
      store.execute(Command.patch(uri, delete, ops.graphToIterable(insert)))

    def getGraph(uri: Rdf#URI): Future[Rdf#Graph] =
      store.execute(Command.get(uri))

    def removeGraph(uri: Rdf#URI): Future[Unit] =
      store.execute(Command.delete(uri))

  }

}
