**This is a work in progress. Sections will be added over time.**

## contributions

All contributions are welcome. By contributing, you accept to give the
ownership of your contribution to the [World Wide Web
Consortium](http://www.w3.org). They are a _nonprofit organization_
and just want to simplify the governance of this opensource project.


## style guide

Your code must look like that:

```scala
package org.w3.banana
package mypackage

import scala.util.{ Try, Success, Failure }
import java.net.{ URI => jURI, _ }
// do not be afraid of wildcards
import java.io._

/** First sentence ending with a period.
 *
 *  Then next paragraph.
 *  And a second line.
 *
 *  And so on.
 */
class Foo[Rdf <: RDF](
  foo: String,
  bar: String
)(implicit
  ops: RDFOps[Rdf],
  writerSelector: RDFWriterSelector[Rdf],
  turtleReader: RDFReader[Rdf, Turtle],
  turtleWriter: RDFWriter[Rdf, Turtle],
  jsonldReader: RDFReader[Rdf, JsonLdCompacted],
  jsonldWriter: RDFWriter[Rdf, JsonLdCompacted]
) extends WordSpec with Matchers {

  val longList = List(
    1,
    "foo"
  )

  val list: List[Any] = List(1, "foo")

  def baz(toto: Int, titi: String = "bazinga")(implicit ev: Show[Int]): Unit = {
    ???
  }

}

/** A much shorter comment, just one line. */
class Foo2[Rdf <: RDF](foo: String, bar: String)(implicit ops: RDFOps[Rdf]) {

}

/** If you need more that one line, all parameters go on their own line. */
class Foo3[Rdf <: RDF](
  foo: String,
  bar: String,
  baz: String
)(implicit
  ops: RDFOps[Rdf]
) {

}

/** Extending classes and traits */
class Foo4[Rdf <: RDF](foo: String, bar: String)(implicit ops: RDFOps[Rdf])
extends Class1 with Trait2 {

}
```

### notes

* the style is subject to change as we learn or need to refine
* new code must follow this standard
* older code should be refactored as it gets worked on
* tools (e.g. scalariform) could help and that's the goal, but it's not there yet

### credits and inspiration

* http://docs.scala-lang.org/style/
* unwritten shapeless coding style e.g. [generic.scala](https://github.com/milessabin/shapeless/blob/master/core/src/main/scala/shapeless/generic.scala)