package org.w3.banana.sesame

import org.w3.banana.diesel._

object SesameDiesel extends Diesel(SesameOperations, SesameGraphUnion, SesameGraphTraversal)
