/*
 *  Copyright (c) 2012 , 2021 W3C Members
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  This program and the accompanying materials are made available under
 *  the W3C Software Notice and Document License (2015-05-13) which is available at
 *  https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document.
 *
 *  SPDX-License-Identifier: W3C-20150513
 */

package org.w3.banana.prefix
import org.w3.banana.{RDF, Ops, PrefixBuilder}

object OWL:
   def apply[T <: RDF](using Ops[T]) = new OWL()

class OWL[T <: RDF](using ops: Ops[T])
    extends PrefixBuilder[T](
      "owl",
      ops.URI("http://www.w3.org/2002/07/owl#")
    ):

   // http://www.w3.org/TR/owl2-rdf-based-semantics/
   // table 3.2: OWL 2 RDF-Based Vocabulary

   val AllDifferent              = apply("AllDifferent")
   val AllDisjointClasses        = apply("AllDisjointClasses")
   val AllDisjointProperties     = apply("AllDisjointProperties")
   val allValuesFrom             = apply("allValuesFrom")
   val annotatedProperty         = apply("annotatedProperty")
   val annotatedSource           = apply("annotatedSource")
   val annotatedTarget           = apply("annotatedTarget")
   val Annotation                = apply("Annotation")
   val AnnotationProperty        = apply("AnnotationProperty")
   val assertionProperty         = apply("assertionProperty")
   val AsymmetricProperty        = apply("AsymmetricProperty")
   val Axiom                     = apply("Axiom")
   val backwardCompatibleWith    = apply("backwardCompatibleWith")
   val bottomDataProperty        = apply("bottomDataProperty")
   val bottomObjectProperty      = apply("bottomObjectProperty")
   val cardinality               = apply("cardinality")
   val Class                     = apply("Class")
   val complementOf              = apply("complementOf")
   val DataRange                 = apply("DataRange")
   val datatypeComplementOf      = apply("datatypeComplementOf")
   val DatatypeProperty          = apply("DatatypeProperty")
   val deprecated                = apply("deprecated")
   val DeprecatedClass           = apply("DeprecatedClass")
   val DeprecatedProperty        = apply("DeprecatedProperty")
   val differentFrom             = apply("differentFrom")
   val disjointUnionOf           = apply("disjointUnionOf")
   val disjointWith              = apply("disjointWith")
   val distinctMembers           = apply("distinctMembers")
   val equivalentClass           = apply("equivalentClass")
   val equivalentProperty        = apply("equivalentProperty")
   val FunctionalProperty        = apply("FunctionalProperty")
   val hasKey                    = apply("hasKey")
   val hasSelf                   = apply("hasSelf")
   val hasValue                  = apply("hasValue")
   val imports                   = apply("imports")
   val incompatibleWith          = apply("incompatibleWith")
   val intersectionOf            = apply("intersectionOf")
   val InverseFunctionalProperty = apply("InverseFunctionalProperty")
   val inverseOf                 = apply("inverseOf")
   val IrreflexiveProperty       = apply("IrreflexiveProperty")
   val maxCardinality            = apply("maxCardinality")
   val maxQualifiedCardinality   = apply("maxQualifiedCardinality")
   val members                   = apply("members")
   val minCardinality            = apply("minCardinality")
   val minQualifiedCardinality   = apply("minQualifiedCardinality")
   val NamedIndividual           = apply("NamedIndividual")
   val NegativePropertyAssertion = apply("NegativePropertyAssertion")
   val Nothing                   = apply("Nothing")
   val ObjectProperty            = apply("ObjectProperty")
   val onClass                   = apply("onClass")
   val onDataRange               = apply("onDataRange")
   val onDatatype                = apply("onDatatype")
   val oneOf                     = apply("oneOf")
   val onProperty                = apply("onProperty")
   val onProperties              = apply("onProperties")
   val Ontology                  = apply("Ontology")
   val OntologyProperty          = apply("OntologyProperty")
   val priorVersion              = apply("priorVersion")
   val propertyChainAxiom        = apply("propertyChainAxiom")
   val propertyDisjointWith      = apply("propertyDisjointWith")
   val qualifiedCardinality      = apply("qualifiedCardinality")
   val ReflexiveProperty         = apply("ReflexiveProperty")
   val Restriction               = apply("Restriction")
   val sameAs                    = apply("sameAs")
   val someValuesFrom            = apply("someValuesFrom")
   val sourceIndividual          = apply("sourceIndividual")
   val SymmetricProperty         = apply("SymmetricProperty")
   val targetIndividual          = apply("targetIndividual")
   val targetValue               = apply("targetValue")
   val Thing                     = apply("Thing")
   val topDataProperty           = apply("topDataProperty")
   val topObjectProperty         = apply("topObjectProperty")
   val TransitiveProperty        = apply("TransitiveProperty")
   val unionOf                   = apply("unionOf")
   val versionInfo               = apply("versionInfo")
   val versionIRI                = apply("versionIRI")
   val withRestrictions          = apply("withRestrictions")
end OWL
