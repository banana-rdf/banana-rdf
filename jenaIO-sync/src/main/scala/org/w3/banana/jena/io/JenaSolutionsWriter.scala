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
//import java.io._
//import org.w3.banana._
//import io._
//import jena._
//import org.apache.jena.riot.ResultSetMgr
//
//import scala.util._
//
//object JenaSolutionsWriter {
//
//  /** Creates a Sparql writer for the given syntax */
//  def apply[T](implicit
//    jenaSparqlSyntax: JenaAnswerOutput[T]
//  ): SparqlSolutionsWriter[Jena, T] = new SparqlSolutionsWriter[Jena, T] {
//
//    def write(answers: Jena#Solutions, os: OutputStream, base: Option[String]) = Try {
//      ResultSetMgr.write(os,answers,jenaSparqlSyntax.formatter)
//    }
//
//    def asString(answers: Jena#Solutions, base: Option[String]): Try[String] = Try {
//      val result = new ByteArrayOutputStream()
//      ResultSetMgr.write(result, answers, jenaSparqlSyntax.formatter)
//      result.toString
//    }
//  }
//
//  implicit val solutionsWriterJson: SparqlSolutionsWriter[Jena, SparqlAnswerJson] =
//    JenaSolutionsWriter[SparqlAnswerJson]
//
//  implicit val solutionsWriterXml: SparqlSolutionsWriter[Jena, SparqlAnswerXml] =
//    JenaSolutionsWriter[SparqlAnswerXml]
//
//}
