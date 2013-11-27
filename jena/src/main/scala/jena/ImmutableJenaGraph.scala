package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Node => JenaNode, TripleMatch => JenaTripleMatch, _ }
import com.hp.hpl.jena.graph.Node.ANY
import com.hp.hpl.jena.shared.{ Command => JenaCommand, _ }
import com.hp.hpl.jena.graph.impl.GraphMatcher
import com.hp.hpl.jena.util.iterator.{ ExtendedIterator, WrappedIterator }
import scala.collection.JavaConverters._
import java.util.{ List => jList, Iterator => jIterator }

object NoUpdateAllowed extends Capabilities {
  def addAllowed(everyTriples: Boolean): Boolean = false
  def addAllowed(): Boolean = false
  def canBeEmpty(): Boolean = true
  def deleteAllowed(everyTriple: Boolean): Boolean = false
  def deleteAllowed(): Boolean = false
  def findContractSafe(): Boolean = true
  def handlesLiteralTyping(): Boolean = true
  def iteratorRemoveAllowed(): Boolean = false
  def sizeAccurate(): Boolean = true
}

object NoEventManager extends GraphEventManager {
  // Members declared in com.hp.hpl.jena.graph.GraphEventManager
  def listening(): Boolean = false
  def notifyAddIterator(x$1: JenaGraph, x$2: jList[Triple]): Unit = ()
  def notifyDeleteIterator(x$1: JenaGraph, x$2: jList[Triple]): Unit = ()
  def register(x$1: GraphListener): GraphEventManager = this
  def unregister(x$1: GraphListener): GraphEventManager = this
  // Members declared in GraphListener
  def notifyAddArray(x$1: JenaGraph, x$2: Array[Triple]): Unit = ()
  def notifyAddGraph(x$1: JenaGraph, x$2: JenaGraph): Unit = ()
  def notifyAddIterator(x$1: JenaGraph, x$2: jIterator[Triple]): Unit = ()
  def notifyAddList(x$1: JenaGraph, x$2: jList[Triple]): Unit = ()
  def notifyAddTriple(x$1: JenaGraph, x$2: Triple): Unit = ()
  def notifyDeleteArray(x$1: JenaGraph, x$2: Array[Triple]): Unit = ()
  def notifyDeleteGraph(x$1: JenaGraph, x$2: JenaGraph): Unit = ()
  def notifyDeleteIterator(x$1: JenaGraph, x$2: jIterator[Triple]): Unit = ()
  def notifyDeleteList(x$1: JenaGraph, x$2: jList[Triple]): Unit = ()
  def notifyDeleteTriple(x$1: JenaGraph, x$2: Triple): Unit = ()
  def notifyEvent(x$1: JenaGraph, x$2: Any): Unit = ()
}

object NoTransactions extends TransactionHandler {
  def abort(): Unit = throw new UnsupportedOperationException
  def begin(): Unit = throw new UnsupportedOperationException
  def commit(): Unit = throw new UnsupportedOperationException
  def executeInTransaction(command: JenaCommand): Object = throw new UnsupportedOperationException
  def transactionsSupported(): Boolean = false
}

class PrefixMappingW(val prefixes: Map[String, String]) extends AnyVal {

  def toPrefixMapping: PrefixMapping = {
    val pm = PrefixMapping.Factory.create()
    prefixes.foreach { case (prefix, uri) => pm.setNsPrefix(prefix, uri) }
    pm
  }

}

case class ImmutableJenaGraph(triples: Set[Jena#Triple], prefixes: Map[String, String]) extends JenaGraph {

  def matchTriples(s: JenaNode, p: JenaNode, o: JenaNode): Iterator[Jena#Triple] = {
    triples.iterator.filter { case JenaOperations.Triple(_s, _p, _o) =>
      (s == null || s == ANY || s == _s) && (p == null || p == ANY || p == _p)  && (o == null || o == ANY || o == _o)
    }
  }

  def add(t: Triple): Unit = new AddDeniedException("ScalaTriples::add")
  def clear(): Unit = new DeleteDeniedException("ScalaTriples::delete")
  def close(): Unit = ()
  def contains(t: Triple): Boolean = triples.contains(t)
  def contains(s: JenaNode, p: JenaNode, o: JenaNode): Boolean = triples.contains(new Triple(s, p, o))
  def delete(t: Triple): Unit = new DeleteDeniedException("ScalaTriples::delete")
  def dependsOn(other: JenaGraph): Boolean = false
  def find(s: JenaNode, p: JenaNode, o: JenaNode): ExtendedIterator[Triple] = {
    val it = matchTriples(s, p, o)
    WrappedIterator.create(it.asJava)
  }
  def find(m: JenaTripleMatch): ExtendedIterator[Triple] = {
    val s = if (m.getMatchSubject == null) ANY else m.getMatchSubject
    val p = if (m.getMatchPredicate == null) ANY else m.getMatchPredicate
    val o = if (m.getMatchObject == null) ANY else m.getMatchObject
    this.find(s, p, o)
  }
  def getBulkUpdateHandler(): BulkUpdateHandler = ??? // deprecated
  def getCapabilities(): Capabilities = NoUpdateAllowed
  def getEventManager(): GraphEventManager = NoEventManager
  def getPrefixMapping(): PrefixMapping = new PrefixMappingW(prefixes).toPrefixMapping
  def getStatisticsHandler(): GraphStatisticsHandler = new GraphStatisticsHandler {
    def getStatistic(s: JenaNode, p: JenaNode, o: JenaNode): Long = matchTriples(s, p, o).size
  }
  def getTransactionHandler(): TransactionHandler = NoTransactions
  def isClosed(): Boolean = true
  def isEmpty(): Boolean = triples.isEmpty
  def isIsomorphicWith(other: JenaGraph): Boolean = GraphMatcher.equals(this, other)
  def remove(s: JenaNode, p: JenaNode, o: JenaNode): Unit = new DeleteDeniedException("this is immutable")
  def size(): Int = triples.size
}

case class WrappedJenaGraph(graph: JenaGraph) extends JenaGraph {
  def add(t: Triple): Unit = new AddDeniedException("ScalaTriples::add")
  def clear(): Unit = new DeleteDeniedException("ScalaTriples::delete")
  def close(): Unit = graph.close()
  def contains(t: Triple): Boolean = graph.contains(t)
  def contains(s: JenaNode, p: JenaNode, o: JenaNode): Boolean = graph.contains(s, p, o)
  def delete(t: Triple): Unit = new DeleteDeniedException("ScalaTriples::delete")
  def dependsOn(other: JenaGraph): Boolean = graph.dependsOn(other)
  def find(s: JenaNode, p: JenaNode, o: JenaNode): ExtendedIterator[Triple] = graph.find(s, p, o)
  def find(m: JenaTripleMatch): ExtendedIterator[Triple] = graph.find(m)
  def getBulkUpdateHandler(): BulkUpdateHandler = ??? // deprecated
  def getCapabilities(): Capabilities = NoUpdateAllowed
  def getEventManager(): GraphEventManager = NoEventManager
  def getPrefixMapping(): PrefixMapping = graph.getPrefixMapping()
  def getStatisticsHandler(): GraphStatisticsHandler = graph.getStatisticsHandler()
  def getTransactionHandler(): TransactionHandler = NoTransactions
  def isClosed(): Boolean = graph.isClosed()
  def isEmpty(): Boolean = graph.isEmpty()
  def isIsomorphicWith(other: JenaGraph): Boolean = graph.isIsomorphicWith(other)
  def remove(s: JenaNode, p: JenaNode, o: JenaNode): Unit = new DeleteDeniedException("this is immutable")
  def size(): Int = graph.size()
}
