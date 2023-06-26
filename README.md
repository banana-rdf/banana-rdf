## Banana-RDF for Scala 3

This project is porting [banana-rdf](https://github.com/banana-rdf/banana-rdf) 
to Scala3. 

Thanks to the financial help by NlNet in the [solid-control](https://github.com/co-operating-systems/solid-control)
project.

### Artifacts

Artifacts can be found in the sonatype repository
https://oss.sonatype.org/content/repositories/snapshots/net/bblfish/rdf/

### Changes from banana-rdf 0.8

[Scala 3](https://docs.scala-lang.org/scala3/) comes with many new features, but dropped one too.

#### Match Types

The most important dropped features we were relying on was type projections. We replace those
with [match types](https://docs.scala-lang.org/scala3/reference/new-types/match-types.html#).
As a result code where we used to have (see [0.8.x GraphTest](../../tree/series/0.8.x/rdf-test-suite/shared/src/main/scala/org/w3/banana/GraphTest.scala#L11))

```scala
val foo1gr: Rdf#Graph = Graph(Triple(exuri(), rdf("foo"), Literal("foo")))
```

we now have ([see GraphTest](rdf-test-suite/shared/src/main/scala/org/w3/banana/GraphTest.scala#L75))

```scala
val foo1gr: Graph[Rdf] = Graph(Triple(exuri(), rdf("foo"), Literal("foo")))
```

#### Relative Uris, Triples and Graphs 

We also make heavy use of [opaque types](https://docs.scala-lang.org/scala3/reference/other-new-features/opaques.html).
This allows us to keep the distinction between relative URLs (`rURI`), triples (`rTriple`) and Graphs (`rGraph`) and 
the usual graphs understood by RDF with absolute urls. The type distinction we make in  [RDF](rdf/shared/src/main/scala/org/w3/banana/RDF.scala) is then also translated by differences in the operations allowed. For example [operations.rGraph](rdf/shared/src/main/scala/org/w3/banana/operations/rGraph.scala) do not allow the union of two rGraphs but do have a method to resolve an rGraph against an AbsoluteUrl. On the other hand the [operations.Graph](rdf/shared/src/main/scala/org/w3/banana/operations/Graph.scala) allow one to take the union of two `Graph[Rdf]`.  They have no method `resolveAgainst` but do have a method `relativizeAgainst` which produces an `rGraph`.

Making relative URLs more clearly visible in the type system makes it easier to write parsers and serialisers, since those do contain
relative URLs.



### Discussions

Some places where ideas have been discussed are:
 * [replacing projection types](https://github.com/lampepfl/dotty/discussions/12527)
 * [Algebras of opaque types and pattern matching](https://contributors.scala-lang.org/t/algebras-of-opaque-types-and-pattern-matching/5245)

### Usage

This is a normal sbt project, you can compile code with `sbt compile` and run it
with `sbt run`, `sbt console` will start a Dotty REPL.
