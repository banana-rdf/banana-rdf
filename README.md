banana-rdf
==========

[![Build Status](https://secure.travis-ci.org/w3c/banana-rdf.png)](http://travis-ci.org/w3c/banana-rdf)

An RDF library in Scala
-----------------------

`banana-rdf` is a generic library for RDF, SPARQL and Linked Data technologies
in Scala.

It can be used with existing libraries without any added cost. There
is no wrapping involved: you manipulate directly the real objects. We
currently support Jena, Sesame and Plantain, a pure Scala
implementation.
Once the code is written, only few lines are required to switch between implementations.
Adding support for other implementations is also easy and can be accomplished by redefining
one or two traits.

Features
--------

`banana-rdf` emphasizes type-safety and immutability, so it can come
with some cost when the underlying implementation is very mutable (I'm
looking at you, Jena and Sesame). We try to keep a clear distinction
between the core concepts and the enhanced syntax that Scala can give
us.

[`RDF`](https://github.com/w3c/banana-rdf/blob/master/rdf/src/main/scala/RDF.scala)
itself is defined as a record of types. Implementations just have to
_plug_ their own types. And because types alone are not enough, we
introduce the
[`RDFOps`](https://github.com/w3c/banana-rdf/blob/master/rdf/src/main/scala/RDFOps.scala)
typeclass, which defines the mandatory operations that an RDF
implementation must
implement. [`SparqlOps`](https://github.com/w3c/banana-rdf/blob/master/rdf/src/main/scala/SparqlOps.scala)
does the same for SPARQL.

With `banana-rdf`, you get `Diesel`, a nice DSL to build and navigate
within **pointed graphs** (graphs with a pointer to an inner
node). You also get an abstraction for **graph stores**
(`GraphStore`), which do not have to be **SPARQL engines**
(`SparqlEngine`). Of course, you can **serialize** and **deserialize**
most of the RDF syntaxes as well as JSON-LD (RDFa will come soon).

`banana-rdf` introduces the concept of **binders**, which let you
bridge the Scala and RDF worlds. Most of the common datastructures are
already available, and you can even map your own classes. Unlike usual
ORM techniques, this does not rely on annotation or reflection.

Core concept or one RDF library to rule 'em all!
-------------------------------------------------

Until we write thorough documentation, the best place to understand
what you can do is to go through the [test
suite](https://github.com/w3c/banana-rdf/tree/master/rdf-test-suite).

When comparing tests of different underlying RDF implementations, like
 [Sesame ](https://github.com/w3c/banana-rdf/blob/master/sesame/src/test/scala/org/w3/banana/sesame/SesameSparqlEngineTest.scala) and
 [Jena](https://github.com/w3c/banana-rdf/blob/master/jena/src/test/scala/org/w3/banana/jena/JenaSparqlEngineTest.scala) it is easy to
  spot that there are only several lines of code there, so all tests (with a lot of complex stuff that is checked there) are defined in
  generic abstract classes that have nothing to do with implementations!
  It reveals the core principle of BananaRDF: `once written, your code will work for all supported RDF libraries with only minor changes
 and you can switch easily whenever you want to or add your own implementation`.

 How was it possible to achieve this?  To add support of your own RDF library you just have to extend:
   [RDF trait](https://github.com/w3c/banana-rdf/blob/master/rdf/common/src/main/scala/org/w3/banana/RDF.scala) ,
    [RDF operations trait](https://github.com/w3c/banana-rdf/blob/master/rdf/common/src/main/scala/org/w3/banana/RDFOps.scala),
    and [Sparql operations trait](https://github.com/w3c/banana-rdf/blob/master/rdf/common/src/main/scala/org/w3/banana/SparqlOps.scala)

 In general there are two common ways of implementing a generic library that supports many implementations
  1) Convince them all to adopt common interfaces. In reality it is hard to achieve as negotiations with libraries maintainers are required
  and often such implementations are not full.
  2) Wrap classes of other libraries into your own classes. In such case no negotiations are needed, but
  wrapping takes efforts and is not good for performance and code clarity.

  'banana-rdf' uses neither of them! By leveraging such cool features of scala like type classes, implicits and type aliases it is possible
  to add support of many implementations without wrapping and negotiations.

As an example let's look how Sesame support is done. RDF trait is a class containing type aliases. Sesame trait just assigns
them to corresponding type aliases, here is part of its code:

  ```scala
    trait Sesame extends RDF {
      // types related to the RDF datamodel
      type Graph = Model
      type Triple = Statement
      type Node = Value
      type URI = SesameURI
      type BNode = SesameBNode
      type Literal = SesameLiteral
      type Lang = String

    // + one some extra lines of code
   }
  ```
Then this class with type aliases is used in so-called "Operation traits". What is interesting about this is that many of them
are written without even knowing what sesame classes are about!
Most of common banana-rdf classes are about supporting different operations that can be done with RDF as well as some syntax support.
Most of them are defined in a generic way:

```scala
trait RDFOps[Rdf <: RDF]
    extends URIOps[Rdf]
    with RDFDSL[Rdf]
    with CommonPrefixes[Rdf]
    with syntax.RDFSyntax[Rdf] {

  // graph

  def emptyGraph: Rdf#Graph

  def makeGraph(it: Iterable[Rdf#Triple]): Rdf#Graph

  def getTriples(graph: Rdf#Graph): Iterable[Rdf#Triple]

  // + some other code
  }
```
Here it is easy to see that RDF class with type is passed. RDFOps has no knowledge of implementation types, it just gets type aliases
 and can only differentiate between triplets, graphs, solutions and manipulates them when it can.
 So, Sesame support includes passing a class that contains type aliases for sesame RDF types and some extra methods for functions
 that cannot be defined without knowing Sesame peculiarities. As type aliases are used, no wrapping is involved,
 everything that is returned is just native Sesame types!

 ```scala
 class SesameOps extends RDFOps[Sesame] with DefaultURIOps[Sesame] {

   val valueFactory: ValueFactory = ValueFactoryImpl.getInstance()

   //a lot of other code with implementations of abstract methods
}
```
With this approach only a small amount of code is required for each RDF implementation and most of the code is generic.
That means that banana team can (and does) have a lot of time for features, so many banana-rdf classes are about adding
additional operations/syntax, DSL and other plugable modules that are generic and thus work for all implementations.



How to start geeking
--------------------

You only need a recent version of Java, that's all:

``` bash
$ git clone git@github.com:w3c/banana-rdf.git
$ cd banana-rdf
$ sbt
```

It's also easy to just build specific target platforms:
    
``` bash
$ sbt +banana_js/test    # for javascript only 
$ sbt +banana_jvm/test   # for jvm only
```

( note: scala-js compilation uses more memory. see [travis.yml](.travis.yml) )

IDE Setup
=========

`banana-rdf` works with both [eclipse](https://www.eclipse.org/) and [IntelliJ IDEA](http://www.jetbrains.com/idea/).

global.sbt
----------
Independent of your preferred IDE, optionally the add the following line to `~/.sbt/0.13/global.sbt` to prevent the 
generation of empty source directories:

```
    unmanagedSourceDirectories in Compile ~= { _.filter(_.exists) }
```

Eclipse
-------
Eclipse should work "out of the box" with the addition of the following global settings:

In `~/.sbt/0.13/global.sbt`:

```
    unmanagedSourceDirectories in Compile ~= { _.filter(_.exists) }
```

In `~/.sbt/0.13/plugins/build.sbt`

```
    addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")
```

To generate eclipse project files, just run the command:

``` bash
$ sbt eclipse
```

IntelliJ IDEA
-------------

IntelliJ IDEA works with just one global change:

In `~/.sbt/0.13/plugins/build.sbt`

```
    addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")
```

To generate IntelliJ project files, just run the command:

``` bash
$ sbt gen-idea
```


Code of Conduct
---------------

**Banana-RDF contributors all agree to follow the [W3C Code of Ethics and Professional Conduct](http://www.w3.org/Consortium/cepc/).**

If you want to take action, feel free to contact Alexandre Bertails <alexandre@bertails.org>. You can also contact W3C Staff as explained in [W3C Procedures](http://www.w3.org/Consortium/pwe/#Procedures).

Contributions
-------------

All contributions are welcome. By contributing, you accept to give the
ownership of your contribution to the [World Wide Web
Consortium](http://www.w3.org). They are a _nonprofit organization_
and just want to simplify the governance of this opensource project.

Before contributing please make sure to copy the [bin/pre-commit](bin/pre-commit)
shell script to the `.git/hooks/` directory of your clone.  This will ensure that all 
your commits are formatted in a consistent way before you push, making it easier
to see the real diffs in a project. On Unix you can do this with the command

```
$ cp bin/pre-commit .git/hooks/
```

Licence
-------

This source code is made available under the [W3C Licence](http://opensource.org/licenses/W3C). This is a business friendly license.
