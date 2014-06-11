package org.w3.banana.ldpatch

import org.w3.banana._
import scala.util.Try
import org.w3.banana.ldpatch.{ model => m }

trait Semantics[Rdf <: RDF] {

  implicit val ops: RDFOps[Rdf]

  import ops._

  object semantics {

    case class State(
      graph: Rdf#Graph,
      varmap: Map[m.Var, Set[Rdf#Node]]
    )

    def LDPatch(patch: m.LDPatch[Rdf], graph: Rdf#Graph): Rdf#Graph = ???

    def Add(add: m.Add[Rdf], state: State): State = {
      val m.Add(s, p, o) = add
      val State(graph, varmap) = state

      ???
    }

    def AddList(addList: m.AddList[Rdf], state: State): State = ???

    def Delete(delete: m.Delete[Rdf], state: State): State = ???

    def VarOrConcrete(vcn: m.VarOrConcrete[Rdf], varmap: Map[m.Var, Set[Rdf#Node]]): Set[Rdf#Node] = vcn match {
      case m.Concrete(node) => Set(node)
      case varr@m.Var(_)    => varmap(varr)
    }

    def Bind(bind: m.Bind[Rdf], state: State): State = {
      val m.Bind(varr, startingNode, path) = bind
      val State(graph, matching) = state
      val nodes = Path(path, graph, VarOrConcrete(startingNode, matching))
      State(graph, matching + (varr -> nodes))
    }

    def Replace(replace: m.Replace[Rdf], state: State): State = ???

    def Path(path: m.Path[Rdf], graph: Rdf#Graph, nodes: Set[Rdf#Node]): Set[Rdf#Node] = {

      def PathElement(pathElem: m.PathElement[Rdf], nodes: Set[Rdf#Node]): Set[Rdf#Node] = pathElem match {
        case m.StepForward(uri) => nodes.flatMap(node => ops.getObjects(graph, node, uri))
        case m.StepBackward(uri) => nodes.flatMap(node => ops.getSubjects(graph, uri, node))
        case m.StepAt(index) =>
          @annotation.tailrec
          def loop(nodes: Set[Rdf#Node], index: Int): Set[Rdf#Node] = index match {
            case 0 => nodes
            case i => loop(nodes.flatMap(node => ops.getObjects(graph, node, ops.rdf.rest)), i-1)
          }
          loop(nodes, index)
        case m.Filter(path, None) => nodes.filter(node => Path(path, graph, nodes).nonEmpty)
        case m.Filter(path, Some(value)) => nodes.filter(node => Path(path, graph, nodes) == Set(value))
        case m.UnicityConstraint => nodes.size match {
          case 0 => sys.error("failed unicity constraint: got empty set")
          case 1 => nodes
          case n => sys.error("failed unicity constraint: got $n matches")
        }
      }

      path.elems.foldLeft(nodes)((nodes, pathElem) => PathElement(pathElem, nodes))

    }

  }


}
