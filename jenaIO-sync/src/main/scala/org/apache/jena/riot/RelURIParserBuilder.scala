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

package org.apache.jena.riot

import org.apache.jena.riot.RDFParser.LangTagForm
import org.apache.jena.riot.RDFParserBuilder
import org.apache.jena.riot.system.{ErrorHandlerFactory, RiotLib}

import java.io.InputStream

class RelURIParserBuilder(
    inputStream: InputStream,
    hintLang: Lang
):

   def build(): RDFParser =
     new RDFParser(
       null,
       null,
       null,
       inputStream,
       null,
       null,
       null,
       null,
       null,
       hintLang,
       hintLang,
       "",
       false,
       java.util.Optional.of(false),
       false,
       LangTagForm.NONE,
       false,
       null,
       null,
       RiotLib.factoryRDF(),
       ErrorHandlerFactory.getDefaultErrorHandler(),
       null
     )
