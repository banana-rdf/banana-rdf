package org.w3.banana.jena.util

import org.apache.jena.datatypes.TypeMapper
import org.apache.jena.query.{QuerySolution as JenaQuerySolution, *}
import org.apache.jena.rdf.model.*
import org.w3.banana.*
import org.w3.banana.jena.*
import org.w3.banana.operations.{BNode, Literal, Node, URI}

class QuerySolution[R <: RDF](using ops: Ops[R]):

   import ops.given

   val modelForBindings = ModelFactory.createDefaultModel().nn

   val typeMapper = TypeMapper.getInstance()

   def toRDFNode(node: RDF.Node[R]): RDFNode =
     node.fold(
       uri => modelForBindings.createResource(uri.value).nn,
       bnode => modelForBindings.createResource(AnonId.create(bnode.label)).nn,
       literal =>
         literal.fold(
           str => modelForBindings.createLiteral(str).nn,
           (str, lang) => modelForBindings.createLiteral(str, lang.label).nn,
           (str, dtType) => modelForBindings.createTypedLiteral(str, dtType.value).nn
         )
     )

   def getMap(bindings: Map[String, RDF.Node[R]]): JenaQuerySolution =
      val map = new QuerySolutionMap()
      bindings.foreach {
        case (name, node) =>
          map.add(name, toRDFNode(node))
      }
      map
