package org.w3.banana.jena

import org.w3.banana.diesel._

object JenaDiesel extends Diesel(JenaOperations, JenaGraphUnion, JenaGraphTraversal)
