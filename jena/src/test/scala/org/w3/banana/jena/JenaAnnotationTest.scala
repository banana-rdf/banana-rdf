package org.w3.banana.jena

import org.w3.banana.meta._
import org.w3.banana.annotations._

class JenaAnnotationTest extends AnnotationTest[Jena]
{
  override def materialize(p: Person): Map[Jena#URI, Any] = Mappable.materializeMappable[Person].toMap[Jena](p)
}
