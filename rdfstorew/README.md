banana-rdfstorew
================

Wrapps [rdfstorew](https://github.com/antoniogarrote/rdfstore-js) with banana-rdf interfaces compiled with [scala-js](http://www.scala-js.org/)  to allow one to use rdfstorew as an implementation of banana-rdf.


compiling
---------

To compile run in sbt

```
> compile
```

For releases  see the [scala-js tutorial](http://www.scala-js.org/doc/tutorial.html)
In short you can use

```
> fastOptJS
```
to produce a large binary. And later optimise it with

```
> fullOptJS
```

though this will require knowing the entry point of the code ( so that the optimisations 
can be calculated from there ).

testing
-------

As with another framework you can run in sbt the command

```
> test
```

