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
//import org.apache.jena.riot.ResultSetMgr
//import org.apache.jena.riot.resultset.ResultSetLang
//
//import java.io.{InputStream, Reader}
//import org.w3.banana._
//import org.w3.banana.io.SparqlQueryResultsReader
//import org.w3.banana.jena.Jena
//import org.w3.banana.io._
//import org.apache.jena.sparql.resultset.SPARQLResult
//
//import scala.util._
//
//abstract private class JenaQueryResultsReader[S] extends SparqlQueryResultsReader[Jena, S] {
//
//  def parse(in: InputStream): SPARQLResult
//
//  def read(in: InputStream, base: String = ""): Try[Either[Solutions[Jena], Boolean]] = Try {
//    val resultSet = parse(in)
//    if (resultSet.isBoolean) {
//      Right(resultSet.getBooleanResult)
//    } else if (resultSet.isResultSet) {
//      Left(resultSet.getResultSet)
//    } else {
//      throw new WrongExpectation("was expecting either a boolean or result set answer. received a model? " + resultSet.isModel)
//    }
//  }
//
//  /**
//   * Note: this is not implemented as Jena does all parsing with InputStreams, so a conversion would
//   * be wasteful
//   **/
//  def read(reader: Reader, base: String): Try[Either[Jena#Solutions, Boolean]] = ???
//
//}
//
//object JenaQueryResultsReader {
//
//  type Answer = Either[Solutions[Jena], Boolean]
//
//  implicit val queryResultsReaderJson: SparqlQueryResultsReader[Jena, SparqlAnswerJson] =
//    new JenaQueryResultsReader[SparqlAnswerJson] {
//      def parse(in: InputStream): SPARQLResult =
//        new SPARQLResult(ResultSetMgr.read(in,ResultSetLang.RS_JSON))
//    }
//
//  implicit val queryResultsReaderXml: SparqlQueryResultsReader[Jena, SparqlAnswerXml] =
//    new JenaQueryResultsReader[SparqlAnswerXml] {
//      def parse(in: InputStream): SPARQLResult =
//        new SPARQLResult(ResultSetMgr.read(in,ResultSetLang.RS_JSON))
//    }
//
//}
