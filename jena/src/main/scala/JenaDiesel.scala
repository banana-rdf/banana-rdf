package org.w3.rdf.jena

import org.w3.rdf.diesel._

object JenaDiesel extends Diesel(JenaOperations, JenaGraphUnion, JenaGraphTraversal)
