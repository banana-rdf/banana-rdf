package org.w3.banana.sesame

import org.w3.banana._

class SesameGraphUnionTest() extends GraphUnionTest[Sesame]()(SesameDiesel, SesameGraphIsomorphism)
