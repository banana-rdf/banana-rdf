package org.w3.banana.sesame

import org.w3.banana._

// why is this implicit not found by Scala??? It is part of the
// companion object for Sesame, just like the other instances...
import Sesame.writerSelector

class SesameJsonLDTest extends JsonLdTest[Sesame]
