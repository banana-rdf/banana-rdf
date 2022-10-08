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

//package org.w3.banana.jena.io
//
//import org.apache.jena.riot.Lang
//import org.apache.jena.riot.resultset.ResultSetLang
//import org.w3.banana.io.*
//
///**
// * typeclass for serialising special
// */
//trait JenaAnswerOutput[T] {
//  def formatter: org.apache.jena.riot.Lang
//}
//
//object JenaAnswerOutput {
//
//  implicit val Json: JenaAnswerOutput[SparqlAnswerJson] =
//    new JenaAnswerOutput[SparqlAnswerJson] {
//      def formatter: Lang =  ResultSetLang.RS_JSON
//    }
//
//  implicit val XML: JenaAnswerOutput[SparqlAnswerXml] =
//    new JenaAnswerOutput[SparqlAnswerXml] {
//      def formatter: Lang = ResultSetLang.RS_JSON
//    }
//
//}
//
