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
      varmap: Map[m.Var, Rdf#Node]
    ) {
      def bind(v: m.Var, node: Rdf#Node): State = this.copy(varmap = varmap + (v -> node))
    }

    object State {
      def apply(graph: Rdf#Graph): State = State(graph, Map.empty)
    }

    /** Computes the length of a `rdf:List`. */
    def length(graph: Rdf#Graph, list: Rdf#Node): Int = {
      lazy val max = graph.size
      @annotation.tailrec
      def loop(current: Rdf#Node, counter: Int): Int = current match {
        case rdf.nil => counter
        case node    =>
          val next = ops.getObjects(graph, node, rdf.rest).headOption.getOrElse(sys.error("malformed list"))
          loop(next, counter + 1)
      }
      loop(list, 0)
    }

    def LDPatch(patch: m.LDPatch[Rdf], graph: Rdf#Graph): Rdf#Graph = {
      patch.statements.foldLeft(State(graph, Map.empty)){ case (state, statement) => Statement(statement, state) }.graph
    }

    def Statement(statement: m.Statement[Rdf], state: State): State = statement match {
      case add@m.Add(_, _)                => Add(add, state)
      case delete@m.Delete(_, _)          => Delete(delete, state)
      case bind@m.Bind(_, _, _)           => Bind(bind, state)
      case cut@m.Cut(_)                   => Cut(cut, state)
      case ul@m.UpdateList(_, _, _, _, _) => UpdateList(ul, state)
    }


    def Add(add: m.Add[Rdf], state: State): State = {
      val m.Add(mode, triples) = add
      val State(graph, varmap) = state
      val f: m.Triple[Rdf] => Rdf#Triple = mode match {
        case m.Lax => { case m.Triple(s, p, o) => Triple(VarOrConcrete(s, varmap), p, VarOrConcrete(o, varmap)) }
        case m.Strict => { case m.Triple(s, p, o) =>
          val groundS = VarOrConcrete(s, varmap)
          val groundO = VarOrConcrete(o, varmap)
          val triple = Triple(groundS, p, groundO)
          if (! ops.find(graph, groundS, p, groundO).isEmpty) sys.error(s"[AddNew] $triple already exists in the graph")
          triple
        }
      }
      val groundTriples: Vector[Rdf#Triple] = triples.map(f)
      State(graph union Graph(groundTriples), varmap)
    }

    def Delete(delete: m.Delete[Rdf], state: State): State = {
      val m.Delete(mode, triples) = delete
      val State(graph, varmap) = state
      val f: m.Triple[Rdf] => Rdf#Triple = mode match {
        case m.Lax => { case m.Triple(s, p, o) => Triple(VarOrConcrete(s, varmap), p, VarOrConcrete(o, varmap)) }
        case m.Strict => { case m.Triple(s, p, o) =>
          val groundS = VarOrConcrete(s, varmap)
          val groundO = VarOrConcrete(o, varmap)
          val triple = Triple(groundS, p, groundO)
          if (ops.find(graph, groundS, p, groundO).isEmpty) sys.error(s"[Delete] $triple did not exist in the graph")
          triple
        }
      }
      val groundTriples: Vector[Rdf#Triple] = triples.map(f)
      State(graph diff Graph(groundTriples), varmap)
    }

    def Cut(cut: m.Cut, state: State): State = {

      val m.Cut(varr) = cut

      val State(graph, varmap) = state

      val bnode = VarOrConcrete(varr, varmap).fold(
        uri => sys.error(s"[Cut] $varr was bound to uri $uri instead of a bnode"),
        bnode => bnode,
        literal => sys.error(s"[Cut] $varr was bound to literal $literal instead of a bnode")
      )

      def isBNode(node: Rdf#Node): Boolean = node.fold(uri => false, bnode => true, literal => false)
      
      @annotation.tailrec
      def loop(bnodes: Vector[Rdf#Node], graph: Rdf#Graph): Rdf#Graph = bnodes match {
        case Vector() => graph
        case bnode +: rest =>
          val outgoingArcs = ops.find(graph, bnode, ANY, ANY).toList
          val newBNodes = outgoingArcs.collect { case Triple(_, _, o) if isBNode(o) => o }
          loop(rest ++ newBNodes, graph diff Graph(outgoingArcs))
      }

      val incomingArcs = ops.find(graph, ANY, ANY, bnode)

      val newGraph = loop(Vector(bnode), graph diff Graph(incomingArcs.toList))

      State(newGraph, varmap)

    }

    def UpdateList(updateList: m.UpdateList[Rdf], state: State): State = {
      val State(graph, varmap) = state

      val m.UpdateList(s, p, slice, headNewList, triples) = updateList

      // the head of the existing list
      val groundS = VarOrConcrete(s, varmap)
      val headList: Rdf#Node = ops.getObjects(graph, groundS, p).to[List] match {
        case Nil         => sys.error(s"[UpdateList] $s $p ?? did not match any triple")
        case head :: Nil => head
        case _           =>  sys.error(s"[UpdateList] $s $p ?? did not match a unique triple")
      }

      val groundTriples: Seq[Rdf#Triple] = triples.map{ case m.Triple(s, p, o) => Triple(VarOrConcrete(s, varmap), p, VarOrConcrete(o, varmap)) }

      val (left, right) = {
        lazy val len = length(graph, headList)
        def positiveIndex(index: Int): Int = {
          val i = if (index < 0) index + len else index
          assert(i >= 0, s"[UpdateList] Index out of bound: $index")
          i
        }
        slice match {
          case m.Range(leftIndex, rightIndex) => (Some(positiveIndex(leftIndex)), Some(positiveIndex(rightIndex)))
          case m.EverythingAfter(index)       => (Some(positiveIndex(index)), None)
          case m.End                          => (None, None)
        }
      }

      @annotation.tailrec
      def step1(s: Rdf#Node, p: Rdf#URI, cursor: Rdf#Node, steps: Option[Int]): (Rdf#Node, Rdf#URI, Rdf#Node) =
        if (steps.exists(_ <= 0) || cursor == rdf.nil) {
          (s, p, cursor)
        } else {
          ops.getObjects(graph, cursor, rdf.rest).to[List] match {
            case Nil         => sys.error(s"[UpdateList/step1] out of rdf:list")
            case next :: Nil =>
              // val elem = ops.getObjects(graph, cursor, rdf.first).headOption.getOrElse(sys.error("[UpdateList/step1] no rdf:first"))
              step1(cursor, rdf.rest, next, steps.map(_ - 1))
            case _           => sys.error("[UpdateList/step1] malformed list: more than one element after bnode rdf:rest")
          }
        }

      val (sLeft, pLeft, oLeft) = step1(groundS, p, headList, left)

      @annotation.tailrec
      def step2(cursor: Rdf#Node, triplesToRemove: List[Rdf#Triple], steps: Option[Int]): (Rdf#Node, List[Rdf#Triple]) =
        if (steps.exists(_ <= 0) || cursor == rdf.nil) {
          (cursor, triplesToRemove)
        } else {
          ops.getObjects(graph, cursor, rdf.rest).to[List] match {
            case Nil         => sys.error(s"[UpdateList/step2] out of rdf:list")
            case next :: Nil =>
              val elem = ops.getObjects(graph, cursor, rdf.first).headOption.getOrElse(sys.error("[UpdateList/step2] no rdf:first"))
              step2(next, Triple(cursor, rdf.first, elem) :: Triple(cursor, rdf.rest, next) :: triplesToRemove, steps.map(_ - 1))
            case _           => sys.error("[UpdateList/step2] malformed list: more than one element after bnode rdf:rest")
          }
        }

      val (headRestList, triplesToRemove) = step2(oLeft, List(Triple(sLeft, pLeft, oLeft)), right.flatMap(r => left.map(l => r - l)))

      val collectionGraph = Graph(groundTriples)

      @annotation.tailrec
      def step3(s: Rdf#Node, p: Rdf#URI, cursor: Rdf#Node): (Rdf#Node, Rdf#URI) = {
        if (cursor == rdf.nil) {
          (s, p)
        } else {
          ops.getObjects(collectionGraph, cursor, rdf.rest).to[List] match {
            // TODO other cases
            case next :: Nil => step3(cursor, rdf.rest, next)
          }
        }
      }

      val (sSlice, pSlice) = step3(sLeft, pLeft, headNewList)

      val newGraph =
        graph union collectionGraph union Graph(Triple(sLeft, pLeft, headNewList)) diff Graph(Triple(sSlice, pSlice, rdf.nil) :: triplesToRemove) union Graph(Triple(sSlice, pSlice, headRestList))

      State(newGraph, varmap)

    }

    def VarOrConcrete(vcn: m.VarOrConcrete[Rdf], varmap: Map[m.Var, Rdf#Node]): Rdf#Node = vcn match {
      case m.Concrete(node) => node
      case varr@m.Var(_)    => varmap(varr)
    }

    def Bind(bind: m.Bind[Rdf], state: State): State = {
      val m.Bind(varr, startingNode, path) = bind
      val State(graph, varmap) = state
      val nodes = Path(path, VarOrConcrete(startingNode, varmap), state)
      nodes.size match {
        case 0 => sys.error(s"$bind didn't match any node")
        case 1 => State(graph, varmap + (varr -> nodes.head))
        case n => sys.error(s"$bind matched $n nodes. Required exactly one 1")
      }
    }

    def Path(path: m.Path[Rdf], startingNode: Rdf#Node, state: State): Set[Rdf#Node] = {

      val State(graph, varmap) = state

      def PathElement(pathElem: m.PathElement[Rdf], nodes: Set[Rdf#Node]): Set[Rdf#Node] = pathElem match {
        case m.StepForward(uri) => nodes.flatMap(node => ops.getObjects(graph, node, uri))

        case m.StepBackward(uri) => nodes.flatMap(node => ops.getSubjects(graph, uri, node))

        case m.StepAt(index) => nodes.flatMap { node =>

          val positiveIndex = if (index < 0) index + length(graph, node) else index
          assert(positiveIndex >= 0, s"[StepAt] Index out of bound: $index")

          @annotation.tailrec
          def loop(node: Rdf#Node, index: Int): Option[Rdf#Node] = index match {
            case 0 => Some(node)
            case i => node match {
              case rdf.nil => None
              case _       =>
                val next = ops.getObjects(graph, node, rdf.rest).headOption.getOrElse(sys.error("[StepAt] malformed list"))
                loop(next, i - 1)
            }
          }

          loop(node, index)
        }

        case m.Filter(path, None) => nodes.filter(node => Path(path, node, state).nonEmpty)

        case m.Filter(path, Some(value)) => nodes.filter { node =>
          val groundValue = VarOrConcrete(value, varmap)
          Path(path, node, state) == Set(groundValue)
        }

        case m.UnicityConstraint => nodes.size match {
          case 0 => sys.error("failed unicity constraint: got empty set")
          case 1 => nodes
          case n => sys.error("failed unicity constraint: got $n matches")
        }

      }

      path.elems.foldLeft(Set(startingNode))((nodes, pathElem) => PathElement(pathElem, nodes))

    }

  }


}
