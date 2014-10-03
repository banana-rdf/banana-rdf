package org.w3.banana.rdfstorew

import org.w3.banana.{ SparqlOps, VarNotFound }

import scala.util.{ Failure, Try, Success }

import scalajs.js

object RDFSparqlOps extends SparqlOps[RDFStore] {
  def SelectQuery(query: String): RDFStore#SelectQuery = query

  def ConstructQuery(query: String): RDFStore#ConstructQuery = query

  def AskQuery(query: String): RDFStore#AskQuery = query

  def UpdateQuery(query: String): RDFStore#UpdateQuery = query

  def Query(query: String): Try[RDFStore#Query] = Success(query)

  def fold[T](query: RDFStore#Query)(select: (RDFStore#SelectQuery) => T,
    construct: (RDFStore#ConstructQuery) => T,
    ask: RDFStore#AskQuery => T) =
    query match {
      case qs: RDFStore#SelectQuery => select(qs)
      case qc: RDFStore#ConstructQuery => construct(qc)
      case qa: RDFStore#AskQuery => ask(qa)
    }

  def getNode(solution: RDFStore#Solution, v: String): Try[RDFStore#Node] = {
    solution match {
      case s: SPARQLSolutionTuple => {
        var node = s(v)
        if (node == null)
          Failure(VarNotFound("var " + v + " not found in BindingSet " + solution.toString))
        else {
          val namedNode = node.asInstanceOf[js.Dynamic].selectDynamic("token").asInstanceOf[String] match {
            case "literal" => {
              val datatype: RDFStoreNamedNode = {
                if (node.asInstanceOf[js.Dynamic].selectDynamic("type").isInstanceOf[Unit] ||
                  node.asInstanceOf[js.Dynamic].selectDynamic("type") == null) {
                  null
                } else {
                  RDFStore.Ops.makeUri(node.asInstanceOf[js.Dynamic].selectDynamic("type").asInstanceOf[String])
                }
              }

              val lang: String = {
                if (node.asInstanceOf[js.Dynamic].selectDynamic("lang").isInstanceOf[Unit] ||
                  node.asInstanceOf[js.Dynamic].selectDynamic("lang") == null) {
                  null
                } else {
                  node.asInstanceOf[js.Dynamic].selectDynamic("lang").asInstanceOf[String]
                }
              }

              if (lang != null) {
                RDFStore.Ops.makeLangTaggedLiteral(
                  node.asInstanceOf[js.Dynamic].selectDynamic("value").asInstanceOf[String],
                  lang
                )
              } else {
                RDFStore.Ops.makeLiteral(
                  node.asInstanceOf[js.Dynamic].selectDynamic("value").asInstanceOf[String],
                  datatype
                )
              }
            }
            case "blank" => {
              RDFStore.Ops.makeBNodeLabel(node.asInstanceOf[js.Dynamic].selectDynamic("value").asInstanceOf[String])
            }
            case "uri" => {
              RDFStore.Ops.makeUri(node.asInstanceOf[js.Dynamic].selectDynamic("value").asInstanceOf[String])
            }
            case _ => throw new Exception("Unknown solution type:'" + node.asInstanceOf[js.Dynamic].selectDynamic("token") + "'")
          }
          Success(namedNode)
        }
      }
      case _ => throw new Exception("SPARQL solution type not implemented yet")
    }
  }

  def varnames(solution: RDFStore#Solution): Set[String] = {
    var s = Set[String]()
    for (key <- js.Object.keys(solution.asInstanceOf[js.Dictionary[Any]]).iterator) { s = s + key }
    s
  }

  def solutionIterator(solutions: RDFStore#Solutions): Iterable[RDFStore#Solution] = solutions

}