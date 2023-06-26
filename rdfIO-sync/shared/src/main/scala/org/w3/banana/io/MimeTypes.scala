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

package org.w3.banana.io

/* Some well-known syntaxes so that we can refer to them in banana-rdf. */

trait N3
trait Turtle
trait NTriples
trait RDFXML
trait RDFaXHTML
trait SparqlAnswerJson
trait SparqlAnswerXml

trait JsonLd
trait JsonLdCompacted extends JsonLd
trait JsonLdExpanded extends JsonLd
trait JsonLdFlattened extends JsonLd
