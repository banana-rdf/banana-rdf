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

    def LDPatch(patch: m.LDPatch[Rdf], graph: Rdf#Graph): Rdf#Graph = {
      patch.statements.foldLeft(State(graph, Map.empty)){ case (state, statement) => Statement(statement, state) }.graph
    }

    def Statement(statement: m.Statement[Rdf], state: State): State = statement match {
      case add@m.Add(_)                => Add(add, state)
      case delete@m.Delete(_)          => Delete(delete, state)
      case bind@m.Bind(_, _, _)        => Bind(bind, state)
      case cut@m.Cut(_)                => Cut(cut, state)
      case ul@m.UpdateList(_, _, _, _) => UpdateList(ul, state)
    }


    def Add(add: m.Add[Rdf], state: State): State = {
      val m.Add(triples) = add
      val State(graph, varmap) = state
      val groundTriples: Vector[Rdf#Triple] = triples.map { case m.Triple(s, p, o) => Triple(VarOrConcrete(s, varmap), p, VarOrConcrete(o, varmap)) }
      State(graph union Graph(groundTriples), varmap)
    }

    def Delete(delete: m.Delete[Rdf], state: State): State = {
      val m.Delete(triples) = delete
      val State(graph, varmap) = state
      val groundTriples: Vector[Rdf#Triple] = triples.map { case m.Triple(s, p, o) => Triple(VarOrConcrete(s, varmap), p, VarOrConcrete(o, varmap)) }
      // TODO should be an error
      State(graph diff Graph(groundTriples), varmap)
    }

    def Cut(cut: m.Cut[Rdf], state: State): State = {
      val m.Cut(node) = cut
      val State(graph, varmap) = state
      val groundNode = VarOrConcrete(node, varmap)
      val incomingArcs = ops.find(graph, ANY, ANY, groundNode)
      @annotation.tailrec
      def loop(nodes: Vector[Rdf#Node], graph: Rdf#Graph): Rdf#Graph = nodes match {
        case Vector() => graph
        case node +: rest =>
          val outcomingArcs = ops.find(graph, node, ANY, ANY).toList
          val newNodes = outcomingArcs.collect { case Triple(_, _, o) if o.fold(uri => true, bnode => true, literal => false) => o }
          loop(rest ++ newNodes, graph diff Graph(outcomingArcs.toList))
      }
      val newGraph = loop(Vector(groundNode), graph diff Graph(incomingArcs.toList))
      State(newGraph, varmap)
    }

    def UpdateList(updateList: m.UpdateList[Rdf], state: State): State = {
      val State(graph, varmap) = state

      val m.UpdateList(s, p, slice, list) = updateList

      val headList: Rdf#Node = ops.getObjects(graph, VarOrConcrete(s, varmap), p).to[List] match {
        case Nil         => sys.error(s"[UpdateList] $s $p ?? did not match any triple")
        case head :: Nil => head
        case _           =>  sys.error(s"[UpdateList] $s $p ?? did not match a unique triple")
      }

      val groundList: Seq[Rdf#Node] = list.map(vc => VarOrConcrete(vc, varmap))

      val groundS = VarOrConcrete(s, varmap)

      val (left, right) = slice match {
        case m.Range(leftIndex, rightIndex) => (Some(leftIndex), Some(rightIndex))
        case m.EverythingAfter(index)       => (Some(index), None)
        case m.End                          => (None, None)
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

      @annotation.tailrec
      def step3(s: Rdf#Node, p: Rdf#URI, headRestList: Rdf#Node, triplesToAdd: List[Rdf#Triple], nodes: Seq[Rdf#Node]): List[Rdf#Triple] = nodes match {

        case Seq() =>
          Triple(s, p, headRestList) :: triplesToAdd
            
        case node +: restNodes =>
          val bnode = BNode()
          step3(bnode, rdf.rest, headRestList, Triple(s, p, bnode) :: Triple(bnode, rdf.first, node) :: triplesToAdd, restNodes)

      }

      val triplesToAdd = step3(sLeft, pLeft, headRestList, List.empty, groundList)

      State(graph.diff(Graph(triplesToRemove)).union(Graph(triplesToAdd)), varmap)

    }

    def UpdateList2(updateList: m.UpdateList2[Rdf], state: State): State = {
      val State(graph, varmap) = state

      val m.UpdateList2(s, p, slice, headNewList, triples) = updateList

      // the head of the existing list
      val groundS = VarOrConcrete(s, varmap)
      val headList: Rdf#Node = ops.getObjects(graph, groundS, p).to[List] match {
        case Nil         => sys.error(s"[UpdateList] $s $p ?? did not match any triple")
        case head :: Nil => head
        case _           =>  sys.error(s"[UpdateList] $s $p ?? did not match a unique triple")
      }

      val groundTriples: Seq[Rdf#Triple] = triples.map{ case m.Triple(s, p, o) => Triple(VarOrConcrete(s, varmap), p, VarOrConcrete(o, varmap)) }

      val (left, right) = slice match {
        case m.Range(leftIndex, rightIndex) => (Some(leftIndex), Some(rightIndex))
        case m.EverythingAfter(index)       => (Some(index), None)
        case m.End                          => (None, None)
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

      val g = Graph(groundTriples)

      @annotation.tailrec
      def step3(s: Rdf#Node, p: Rdf#URI, cursor: Rdf#Node): (Rdf#Node, Rdf#URI) = {
        println("cursor: " + cursor)
        if (cursor == rdf.nil) {
          (s, p)
        } else {
          ops.getObjects(g, cursor, rdf.rest).to[List] match {
            // TODO other cases
            case next :: Nil => step3(cursor, rdf.rest, next)
          }
        }
      }

      val (sSlice, pSlice) = step3(sLeft, pLeft, headNewList)

      val newGraph =
        graph union Graph(groundTriples :+ Triple(sLeft, pLeft, headNewList)) diff Graph(Triple(sSlice, pSlice, rdf.nil) :: triplesToRemove) union Graph(Triple(sSlice, pSlice, headRestList))

      val r = """[-\w]*:[-\w]*:([-\w]*)""".r

      implicit class S(val s: String) {
        def clean: String = r.replaceAllIn(s, m => m group 1)
      }

      println("sLeft: " + sLeft.toString.clean)
      println("pLeft: " + pLeft.toString.clean)
      println("add triples from the new slice/collection: " + groundTriples.toString.clean)
      println("connect left side with head of new list: " + Triple(sLeft, pLeft, headNewList).toString.clean)
      println("remove last triple in the slice pointing to nil: " + Triple(sSlice, pSlice, rdf.nil).toString.clean)
      println("remove triples from the slice: " + triplesToRemove.toString.clean)
      println("connect end of slice to head of the rest of the first list: " + Triple(sSlice, pSlice, headRestList).toString.clean)

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

        case m.StepAt(index) =>
          @annotation.tailrec
          def loop(nodes: Set[Rdf#Node], index: Int): Set[Rdf#Node] = index match {
            case 0 => nodes
            case i => loop(nodes.flatMap(node => ops.getObjects(graph, node, ops.rdf.rest)), i-1)
          }
          loop(nodes, index)

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
