package org.w3.banana.sesame

import org.w3.banana._

object SesameGraphTest extends GraphTest[Sesame]

object SesameMGraphTest extends MGraphTest[Sesame]

import org.w3.banana.isomorphism._

object SesameIsomorphismTest extends IsomorphismTest[Sesame]

object SesameGraphUnionTest extends GraphUnionTest[Sesame]

object SesamePointedGraphTest extends PointedGraphTest[Sesame]

import org.w3.banana.diesel._

object SesameDieselGraphConstructTest extends DieselGraphConstructTest[Sesame]

object SesameDieselGraphExplorationTest extends DieselGraphExplorationTest[Sesame]
