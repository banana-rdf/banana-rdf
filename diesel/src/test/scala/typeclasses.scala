package org.w3.rdf.diesel


import org.w3.rdf.jena._

object JenaDiesel extends Diesel(JenaOperations, JenaGraphUnion, JenaGraphTraversal)


import org.w3.rdf.sesame._

object SesameDiesel extends Diesel(SesameOperations, SesameGraphUnion, SesameGraphTraversal)
