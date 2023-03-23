## Banana-Play

This is a project to port [banana-rdf](https://github.com/banana-rdf/banana-rdf) 
to Scala3. [Scala 3](https://docs.scala-lang.org/scala3/) comes with many new features, and dropped one
of the most important ones we were relying on: type projections. 
So this repository is trying to build up from scratch the framework,
to see what holds water, before moving it back to the main repository.

The main work on this branch is going on in the [bblfish banana-rdf](https://github.com/bblfish/banana-rdf) repository. Check the pull requests there to see what the latest branch being worked on is.

### Discussions

Some places where ideas have been discussed are:
 * [replacing projection types](https://github.com/lampepfl/dotty/discussions/12527)
 * [Algebras of opaque types and pattern matching](https://contributors.scala-lang.org/t/algebras-of-opaque-types-and-pattern-matching/5245)

### Usage

This is a normal sbt project, you can compile code with `sbt compile` and run it
with `sbt run`, `sbt console` will start a Dotty REPL.
