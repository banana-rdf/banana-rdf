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

package org.w3.banana

import org.w3.banana.TestConstants.foafPre

import org.w3.banana.RDF.*
import TestConstants.*

object TestConstants:
   def bbl(id: String): String = "https://bblfish.net/" + (if id == "" then "" else "#" + id)
   def bfsh(path: String, id: String): String = "https://bblfish.net/" + path + "#" + id
   def tim(id: String): String =
     "https://www.w3.org/People/Berners-Lee/card" + (if id == "" then "" else "#" + id)

   def foafPre(id: String): String = "http://xmlns.com/foaf/0.1/" + id
   def xsdPre(id: String): String = "http://www.w3.org/2001/XMLSchema#" + id
