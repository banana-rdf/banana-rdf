package org.w3.banana.jena.util

import org.w3.banana._
import org.w3.banana.jena._
import org.w3.banana.jena.JenaOperations._
import com.hp.hpl.jena.rdf.model._
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.datatypes.TypeMapper
import com.hp.hpl.jena.rdf.model.ModelFactory.createModelForGraph

case class QuerySolution() {

  val modelForBindings = ModelFactory.createDefaultModel()

  val typeMapper = TypeMapper.getInstance()

  // oh my, what have I done to deserve this?
  def toRDFNode(node: Jena#Node): RDFNode = foldNode(node)(
    { case URI(str) => modelForBindings.createResource(str) },
    { case BNode(label) => modelForBindings.createResource(AnonId.create(label)) },
    { literal =>
      foldLiteral(literal) (
        { case TypedLiteral(lexicalForm, URI(datatype)) => modelForBindings.createTypedLiteral(lexicalForm, typeMapper.getSafeTypeByName(datatype)) },
        { case LangLiteral(lexicalForm, Lang(lang)) => modelForBindings.createLiteral(lexicalForm, lang) }
      )
    }
  )

  def getMap(bindings: Map[String, Jena#Node]): QuerySolutionMap = {
    val map = new QuerySolutionMap()
    bindings foreach {
      case (name, node) =>
        map.add(name, toRDFNode(node))
    }
    map
  }

}
