package org.w3.banana.sesame.io

import org.w3.banana.io.JsonLdExtendedTest
import org.w3.banana.sesame.Sesame
import org.w3.banana.util.tryInstances._

import scala.util.Try

// why is this implicit not found by Scala??? It is part of the
// companion object for Sesame, just like the other instances...
import org.w3.banana.sesame.Sesame.writerSelector

class SesameJsonLDTest extends JsonLdExtendedTest[Sesame, Try]
