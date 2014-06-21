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
      case add@m.Add(_, _, _)          => Add(add, state)
      case addList@m.AddList(_, _, _)  => AddList(addList, state)
      case delete@m.Delete(_, _, _)    => Delete(delete, state)
      case bind@m.Bind(_, _, _)        => Bind(bind, state)
      case ul@m.UpdateList(_, _, _, _) => UpdateList(ul, state)
    }


    def Add(add: m.Add[Rdf], state: State): State = {
      val m.Add(s, p, o) = add
      val State(graph, varmap) = state
      val groundTriple = Triple(VarOrConcrete(s, varmap), p, VarOrConcrete(o, varmap))
      State(graph + groundTriple, varmap)
    }

    def AddList(addList: m.AddList[Rdf], state: State): State = {
      val m.AddList(s, p, list) = addList
      val State(graph, varmap) = state
      @annotation.tailrec
      def loop(s: m.VarOrConcrete[Rdf], p: Rdf#URI, list: Seq[m.VarOrConcrete[Rdf]], acc: Set[Rdf#Triple]): Set[Rdf#Triple] = list match {
        case Seq() =>
          acc + Triple(VarOrConcrete(s, varmap), p, rdf.nil)
        case head +: rest =>
          val bnode = BNode()
          val newAcc = acc + Triple(VarOrConcrete(s, varmap), p, bnode) + Triple(bnode, rdf.first, VarOrConcrete(head, varmap))
          loop(m.Concrete(bnode), rdf.rest, rest, newAcc)
      }
      val triples = loop(s, p, list, Set.empty)
      State(graph union Graph(triples), varmap)
    }

    def foo(node: Rdf#Node, graph: Rdf#Graph): String = {
      val s = ops.foldNode(node)(
        uri => uri.toString,
        bnode => {
           ops.getObjects(graph, bnode, rdf.first).to[List] match {
             case Nil => s"[$bnode]"
             case l :: Nil => s"[$bnode/rdf:first = $l]"
             case a :: b :: _ => s"[$bnode/malformed rdf:first = $a $b]"
           }
        },
        literal => literal.toString
      )
      s
//      println(s">> $s")
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

      @annotation.tailrec
      def loop(s: Rdf#Node, p: Rdf#URI, cursor: Rdf#Node, acc: List[Rdf#Triple], steps: Option[Int]): (List[Rdf#Triple], Rdf#Triple) =
        if (steps.exists(_ <= 0) || cursor == rdf.nil) {
          println("[loop] stopped at " + foo(cursor, graph))
          (acc, Triple(s, p, cursor))
        } else {
          ops.getObjects(graph, cursor, rdf.rest).to[List] match {
            case Nil         => sys.error(s"[UpdateList] out of rdf:list")
            case next :: Nil =>
              val elem = ops.getObjects(graph, cursor, rdf.first).headOption.getOrElse(sys.error("[UpdateList] no rdf:first"))
              println("[loop] currently at " + foo(cursor, graph))
              loop(cursor, rdf.rest, next, Triple(s, p, cursor) :: Triple(cursor, rdf.first, elem) :: acc, steps.map(_ - 1))
            case _           => sys.error("[UpdateList] malformed list: more than one element after bnode rdf:rest")
          }

        }

      @annotation.tailrec
      def makeList(s: Rdf#Node, p: Rdf#URI, o: Rdf#Node, nodes: Seq[Rdf#Node], acc: List[Rdf#Triple]): List[Rdf#Triple] = {

        println(s"## $s $p $o")
        nodes match {
          case Seq() =>
            Triple(s, p, o) :: acc
          case node +: restNodes =>
            val bnode = BNode()
            makeList(bnode, rdf.rest, o, restNodes, Triple(s, p, bnode) :: Triple(bnode, rdf.first, node) :: acc)
        }
      }

      val (left, right) = slice match {
        case m.Range(leftIndex, rightIndex) => (Some(leftIndex), Some(rightIndex))
        case m.EverythingAfter(index)       => (Some(index), None)
        case m.End                          => (None, None)
      }
      println("@@ moving for Left")
      val (_, Triple(sLeft, pLeft, oLeft)) = loop(groundS, p, headList, List.empty, left)

      println("sLeft = "+foo(sLeft, graph))
      println("oLeft = "+foo(oLeft, graph))

      println("@@ moving for Right")
      val (betweenTriples, Triple(sRight, pRight, oRight)) = loop(sLeft, pLeft, oLeft, List.empty, right.flatMap(r => left.map(l => r - l)))
      println("sRight = "+foo(sRight, graph))
      println("oRight = "+foo(oRight, graph))

      println("betweenTriples = "+betweenTriples)

      val listTriples = makeList(sLeft, pLeft, sRight, groundList, List.empty)
      println("listTriples = " + listTriples)

      State(graph.diff(Graph(betweenTriples)).union(Graph(listTriples)), varmap)

    }


    def Delete(delete: m.Delete[Rdf], state: State): State = {
      val m.Delete(s, p, o) = delete
      val State(graph, varmap) = state
      val groundTriple = Triple(VarOrConcrete(s, varmap), p, VarOrConcrete(o, varmap))
      State(graph diff Graph(groundTriple), varmap)
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
