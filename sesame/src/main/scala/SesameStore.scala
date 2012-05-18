package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.model._
import org.openrdf.model.impl._
import org.openrdf.repository._
import scala.collection.JavaConverters._
import org.openrdf.query._
import org.openrdf.rio.RDFHandler
import SesameUtil.withConnection

import org.openrdf.repository._

case class SesameStore(store: Repository)
extends RDFStore[Sesame, SesameSPARQL]
with SesameGraphStore
with SesameSPARQLEngine
