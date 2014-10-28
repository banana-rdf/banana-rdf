package org.w3.banana.sesame.io

import org.w3.banana._
import org.w3.banana.io.JsonLdTest
import org.w3.banana.sesame.Sesame
import scala.util.Try
import org.w3.banana.util.tryInstances._

// why is this implicit not found by Scala??? It is part of the
// companion object for Sesame, just like the other instances...
import org.w3.banana.sesame.Sesame.writerSelector

class SesameJsonLDTest extends JsonLdTest[Sesame, Try]
