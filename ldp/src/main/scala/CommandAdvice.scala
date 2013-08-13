package org.w3.banana.ldp

import org.w3.banana._
import org.openrdf.model.Graph

/**
 * Advice ( http://en.wikipedia.org/wiki/Advice_in_aspect-oriented_programming ) to be applied before or after
 * application of a Command
 */
trait CommandAdvice[Rdf<:RDF] {
  /**
   * run some pre-conditions on an ldp ldp command
   *
   * @param cmd the initial command
   * @tparam A
   * @return Some throwable if the command should not continue.
   * // a more java way would just be to throw the exception and declare that it does.
   * // does it help to just return it?
   */
   def pre[A](cmd: LDPCommand[Rdf, LDPCommand.Script[Rdf,A]]): Option[Throwable]

  /**
   * run some actions after executing the command.
   *
   * @param cmd the initial command - todo: it may be useful to also get the resulting command, built up
   *            during the command execution, so that if needed it can be flatmapped on the other ( in
   *            order to get commands that don't get run in parallel.
   * @tparam A
   */
   def post[A](cmd: LDPCommand[Rdf, LDPCommand.Script[Rdf,A]])
}

/**
 * An Advice Selector is a function that takes a graph, and can find CommandAdvices to apply for the given resource
 * //todo: it should be a function from LDPResource to command advice so that metadata on the resource is
 * taken into account.
 * @tparam Rdf
 */
trait AdviceSelector[Rdf<:RDF] extends Function1[NamedResource[Rdf],Iterable[CommandAdvice[Rdf]]]

class EmptyAdviceSelector[Rdf<:RDF] extends AdviceSelector[Rdf]{
  def apply(v1: NamedResource[Rdf]) = Iterable.empty
}

/**
 * try building an advice selector to test the interface
 * A better implementation would read the mappings from Resource types to scala AdviceSelectors from
 * a config file.
 * @param ops
 * @tparam Rdf
 */
class TestAdviceSelector[Rdf<:RDF](implicit ops: RDFOps[Rdf]) extends AdviceSelector[Rdf] {

  import ops._
  import org.w3.banana.diesel._
  import syntax._
  val sioc = SiocPrefix[Rdf]

  def apply(namedResource: NamedResource[Rdf]) = {
    val resourceGraph = namedResource match {
      case ldpr: LDPR[Rdf] => PointedGraph(ldpr.location,ldpr.graph)
      case other => other.meta
    }
    for (tp <- resourceGraph / rdf.typ) yield {
      tp.pointer match { // here we select on various types of resources
        case sioc.Container => new CommandAdvice[Rdf] {
          def pre[A](cmd: LDPCommand[Rdf, LDPCommand.Script[Rdf, A]]) = {
             cmd match {
               case CreateLDPR(container,_,graph,k) => {
                 val passes = (PointedGraph(container,graph.resolveAgainst(container))/rdf.typ).exists{ pg =>
                    pg.pointer == sioc.Item || pg.pointer == sioc.Post
                 }
                 if (!passes)
                   Some(WrongExpectation("One can only Post sioc:Item and so sioc:Posts to a sioc:Container that is an ldp:Container"))
                 else None
               }
               //what other things would one want to test ?
               case _ => None
             }
          }
          def post[A](cmd: LDPCommand[Rdf, LDPCommand.Script[Rdf, A]]) {
             cmd match {
               case CreateLDPR(container,_,graph,k) => {
                 //we should look into the resourceGraph to check if there is a link to a list of listeners
                 //and if there is we should send each of them notifications. The notifications can be sent
                 //to an event bus perhaps, so that requests can be sent even if the server is down.
                 // perhaps this means we need a wrapper for an case class DoLater(cmd: LDPCommand, started: Date, ...)
                 // for commands that should be tried a number of times until success?

                 // in fact we may want to do this for any altering request.
               }
             }
          }
        }
      }
    }
  }
}
