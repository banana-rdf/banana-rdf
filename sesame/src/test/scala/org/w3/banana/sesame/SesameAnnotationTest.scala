package org.w3.banana.sesame

import org.w3.banana.meta._
import org.w3.banana.annotations._


class SesameAnnotationTest extends AnnotationTest[Sesame]{
  override def materialize(p: Person): Map[Sesame#URI, Any] = Mappable.materializeMappable[Person].toMap[Sesame](p)
}
