package org.w3.rdf.sesame

import org.w3.rdf.diesel._

object SesameDiesel extends Diesel(SesameOperations, SesameGraphUnion, SesameGraphTraversal)
