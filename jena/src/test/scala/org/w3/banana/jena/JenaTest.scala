package org.w3.banana.jena

import org.w3.banana._
import org.w3.banana.isomorphism.IsomorphismTests

object JenaGraphTest extends GraphTest[Jena]

object JenaMGraphTest extends MGraphTest[Jena]

class JenaIsomorphismTest extends IsomorphismTests[Jena]

object JenaGraphUnionTest extends GraphUnionTest[Jena]
