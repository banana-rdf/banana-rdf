Pimp my RDF
===========

An RDF library in Scala
-----------------------

The goals are:

* to follow **carefully** the [RDF model](http://www.w3.org/TR/rdf11-concepts/#section-Graph-syntax)
* to be fully compatible with existing RDF Java libraries like Jena and Sesame
* to be type-safe
* to provide a simple yet powerful API to manipulate RDF

The library itself makes use of interesting Scala features like [dependent method types](http://stackoverflow.com/a/7861070/1057315), [singleton types](http://stackoverflow.com/questions/4315678/how-to-use-scalas-singleton-object-types) and [typeclasses](http://debasishg.blogspot.com/2010/06/scala-implicits-type-classes-here-i.html).

How to start geeking
--------------------

You only need a recent version of Java, that's all:

    $ git clone git@github.com:betehess/pimp-my-rdf.git
    $ cd pimp-my-rdf
    $ ./sbt


Licence
-------

This source code is licenced under an open source license. 
Whether this licence be 

 * [W3C Licence](http://opensource.org/licenses/W3C)
 * [MIT Licence](http://opensource.org/licenses/MIT)
 * [Apache 2.0 Licence](http://opensource.org/licenses/Apache-2.0)

has not yet been decided
