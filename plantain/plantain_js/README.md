banana-pome
===========

A pure [scala-js](http://www.scala-js.org/) RDF Store based on Plantain.

A [pome banana](http://www.bananas.org/f2/dwarf-brazilian-vs-unidentified-pome-18078.html) is a small banana.
![image](https://cloud.githubusercontent.com/assets/124506/3559590/c95e1ff0-094c-11e4-9a47-5703c8ecc6ce.png)
As per [pro-musa definition](http://www.promusa.org/Pome+subgroup) the main characteristics of the fruit
are that it is small and that

```
The fruit bunch develops at an angle while the rachis points vertically down. The fruit apex is often bottlenecked.
```

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

