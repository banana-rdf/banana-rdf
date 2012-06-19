import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.memory.MemoryStore
import org.openrdf.sail.Sail
import org.w3.banana.{Row, RDFGraphQuery}
import org.w3.banana.sesame.{SesameStore, Sesame, SesameSPARQL}
import scalaz.Failure

/**
 * Sesame Graph Queries are better made on stores, if multiple queries need
 * to be made on the same graph. This class needs to map the graph to the store
 * for each query.
 *
 * arguably SesameGraphQuery should be a case class
 *    SesameGraphQuery(graph)
 * and indeed this should extend to RDFGraphQuery(graph)
 *
 */
object SesameGraphQuery extends RDFGraphQuery[Sesame, SesameSPARQL] {

  /**
   * Sesame queries can only be made on stores, hence this is needed.
   * @param graph
   */
  protected def makeStore(graph: Sesame#Graph) = {
    val store = new MemoryStore
    val sail = new SailRepository(store)
    val sailconn = sail.getConnection
    sailconn.add(graph)
    SesameStore(sail)
  }

  def executeSelect(graph: Sesame#Graph, query: SesameSPARQL#SelectQuery): Iterable[Row[Sesame]] = {
    makeStore(graph).executeSelect(query)
  }

  def executeConstruct(graph: Sesame#Graph, query: SesameSPARQL#ConstructQuery) = {
    makeStore(graph).executeConstruct(query)
  }

  def executeAsk(graph: Sesame#Graph, query: SesameSPARQL#AskQuery) = {
    makeStore(graph).executeAsk(query)

  }
}
