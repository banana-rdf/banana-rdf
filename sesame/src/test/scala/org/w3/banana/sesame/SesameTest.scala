package org.w3.banana.sesame

import org.w3.banana._
import org.w3.banana.isomorphism.IsomorphismTests

object SesameGraphTest extends GraphTest[Sesame]

object SesameMGraphTest extends MGraphTest[Sesame]

class SesameIsomorphismTest extends IsomorphismTests[Sesame]

object SesameGraphUnionTest extends GraphUnionTest[Sesame]
