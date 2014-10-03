package org.w3.banana.jena.util

import com.hp.hpl.jena.datatypes.TypeMapper
import com.hp.hpl.jena.query.{ QuerySolution => JenaQuerySolution, _ }
import com.hp.hpl.jena.rdf.model._
import org.w3.banana._
import org.w3.banana.jena._

class QuerySolution(ops: RDFOps[Jena]) {

  import ops._

  val modelForBindings = ModelFactory.createDefaultModel()

  val typeMapper = TypeMapper.getInstance()

  // oh my, what have I done to deserve this?
  def toRDFNode(node: Jena#Node): RDFNode = foldNode(node)(
    { case URI(str) => modelForBindings.createResource(str) },
    { case BNode(label) => modelForBindings.createResource(AnonId.create(label)) },
    {
      case Literal(lexicalForm, URI(datatype), None) => modelForBindings.createTypedLiteral(lexicalForm, typeMapper.getSafeTypeByName(datatype))
      case Literal(lexicalForm, _, Some(Lang(lang))) => modelForBindings.createLiteral(lexicalForm, lang)
    }
  )

  def getMap(bindings: Map[String, Jena#Node]): JenaQuerySolution = {
    val map = new QuerySolutionMap()
    bindings foreach {
      case (name, node) =>
        map.add(name, toRDFNode(node))
    }
    map
  }

}
