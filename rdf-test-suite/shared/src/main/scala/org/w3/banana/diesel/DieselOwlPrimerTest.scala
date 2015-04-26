package org.w3.banana.diesel

import org.scalatest.{Matchers, WordSpec}
import org.w3.banana._


//Todo: ToGraph should be extracted - defining union on pointed graphs doesn't really make sense but still, this pattern is useful.
object ToGraph {

  implicit def toGraph[Rdf <: RDF](s:Iterable[PointedGraph[Rdf]])(implicit ops: RDFOps[Rdf]):Rdf#Graph =
    ops.makeGraph(s.map(pg => ops.getTriples(pg.graph)).flatten)
}

class FAMILYPrefix[Rdf <: RDF](ops: RDFOps[Rdf]) extends PrefixBuilder("families", "http://example.com/owl/families/")(ops) {

  // T-Box
  // Classes
  val Person           = apply("Person")
  val Woman            = apply("Woman")
  val Parent           = apply("Parent")
  val Father           = apply("Father")
  val Grandfather      = apply("Grandfather")
  val Mother           = apply("Mother")
  val SocialRole       = apply("SocialRole")
  val Man              = apply("Man")
  val Teenager         = apply("Teenager")
  val ChildlessPerson  = apply("ChildlessPerson")
  val Human            = apply("Human")
  val Female           = apply("Female")
  val HappyPerson      = apply("HappyPerson")
  val JohnsChildren    = apply("JohnsChildren")
  val NarcisticPerson  = apply("NarcisticPerson")
  val MyBirthdayGuests = apply("MyBirthdayGuests")
  val Dead             = apply("Dead")
  val Orphan           = apply("Orphan")
  val Adult            = apply("Adult")
  val YoungChild       = apply("YoungChild")

  // Object Properties
  val hasWife        = apply("hasWife")
  val hasChild       = apply("hasChild")
  val hasDaughter    = apply("hasDaughter")
  val loves          = apply("loves")
  val hasRelative    = apply("hasRelative")
  val hasSpouse      = apply("hasSpouse")
  val hasGrandparent = apply("hasGrandparent")
  val hasParent      = apply("hasParent")
  val parentOf       = apply("parentOf")
  val hasBrother     = apply("hasBrother")
  val hasFather      = apply("hasFather")
  val hasUncle       = apply("hasUncle")
  val hasSon         = apply("hasSon")
  val hasAncestor    = apply("hasAncestor")
  val hasHusband     = apply("hasHusband")

  // DataProperty
  val hasAge = apply("hasAge")
  val hasSSN = apply("hasSSN")

  //Datatype
  val personAge  = apply("personAge")
  val minorAge   = apply("minorAge")
  val majorAge   = apply("majorAge")
  val toddlerAge = apply("toddlerAge")

  // A-Box
  // Named Individuals
  val John   = apply("John")
  val Mary   = apply("Mary")
  val Jim    = apply("Jim")
  val James  = apply("James")
  val Jack   = apply("Jack")
  val Bill   = apply("Bill")
  val Meg    = apply("Meg")
  val Susan  = apply("Susan")
}

object FAMILYPrefix {
  def apply[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FAMILYPrefix(ops)
}

/**
 * This test uses the W3C owl primer example to provide a use case test for Diesel. 
 *
 * Owl2:
 *  - Is the Web Ontology Language
 *  - Extends the semantics of the description logic SROIQ
 *  - Has a structural specification that is independent of the concrete exchange syntaxes for OWL 2 ontologies
 *  - Has a few, W3C defined mappings to exchange syntaxes, including RDF, Functional and Manchester Syntax
 *
 */
class DieselOwlPrimerTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends WordSpec with Matchers {
  import ToGraph._
  import ops._

  import scala.language.postfixOps

  val owl      = OWLPrefix[Rdf]
  val rdfs     = RDFSPrefix[Rdf]
  val otherOnt = Prefix("otherOnt", "http://example.org/otherOntologies/families/")

  val f   = FAMILYPrefix[Rdf]

  object Owl2{

    def equivalentClasses(ce:Rdf#URI,pg:PointedGraph[Rdf]):PointedGraph[Rdf] = ce -- owl.equivalentClass ->- pg

    def objectComplementOf(ce:Rdf#URI):PointedGraph[Rdf] = (
      bnode -- rdf.typ          ->- owl.Class
            -- owl.complementOf ->- ce
      )
    def ¬(ce:Rdf#URI):PointedGraph[Rdf] = objectComplementOf(ce)

    def objectIntersectionOf(ce:PointedGraph[Rdf]*):PointedGraph[Rdf] = (
      bnode() -- rdf.typ          ->- owl.Class
              -- owl.intersectionOf ->- ce.toList
      )

    implicit def uriToPG(u:Rdf#URI):PointedGraph[Rdf] = u.toPG
  }

  implicit class UriOps(ce:Rdf#URI) {
    def equivalentClasses(pg:PointedGraph[Rdf]):PointedGraph[Rdf] = Owl2.equivalentClasses(ce, pg)
    def ≡(pg:PointedGraph[Rdf]):PointedGraph[Rdf] = equivalentClasses(pg)

    def objectComplementOf():PointedGraph[Rdf] = Owl2.objectComplementOf(ce)
    def ¬():PointedGraph[Rdf] = objectComplementOf()

    def objectIntersectionOf(pg:PointedGraph[Rdf]):PointedGraph[Rdf] = Owl2.objectIntersectionOf(ce.toPG, pg )
    def ⊓(pg:PointedGraph[Rdf]):PointedGraph[Rdf] = objectIntersectionOf(pg)

  }

  implicit class PgOps(p:PointedGraph[Rdf]) {
  /*  def equivalentClasses(pg:PointedGraph[Rdf]):PointedGraph[Rdf] = Owl2.equivalentClasses(ce, pg)
    def ≡(pg:PointedGraph[Rdf]):PointedGraph[Rdf] = equivalentClasses(pg)

    def objectComplementOf( ):PointedGraph[Rdf] = Owl2.objectComplementOf(ce)
    def ¬():PointedGraph[Rdf] = objectComplementOf
*/
    def objectIntersectionOf(pg:PointedGraph[Rdf]):PointedGraph[Rdf] = Owl2.objectIntersectionOf(p, pg )
    def ⊓(pg:PointedGraph[Rdf]):PointedGraph[Rdf] = objectIntersectionOf(pg)
  }

  "Diesel/Owl must support class expressions of the form: ObjectComplementOf " in {

    /**
      #Functional-Style Syntax
      EquivalentClasses(
        :ChildlessPerson
        ObjectIntersectionOf(
          :Person
          ObjectComplementOf( :Parent )
        )
      )

      #Turtle Syntax
      :ChildlessPerson  owl:equivalentClass  [
        rdf:type            owl:Class ;
        owl:intersectionOf  ( :Person
                              [ rdf:type          owl:Class ;
                                owl:complementOf  :Parent ] )
      ] .
     */

    import Owl2._

    val g:Rdf#Graph = Seq (
        f.ChildlessPerson -- owl.equivalentClass ->- ( bnode
        -- rdf.typ            ->- owl.Class
        -- owl.intersectionOf ->- List( f.Person.toPG,
                                        bnode -- rdf.typ          ->- owl.Class
                                              -- owl.complementOf ->- f.Parent))
    )

    // owl Funcion Graph
    val fg  = equivalentClasses(f.ChildlessPerson, objectIntersectionOf( f.Person, objectComplementOf(f.Parent)))

    // Scala Graph
    val sg  = f.ChildlessPerson ≡ (f.Person ⊓ (f.Parent¬))
    val sg2 = f.ChildlessPerson ≡ (f.Person ⊓ ¬(f.Parent))

    val expectedGraph = Graph(
      Triple(URI("http://example.com/owl/families/ChildlessPerson"), URI("http://www.w3.org/2002/07/owl#equivalentClass"), bnode("a")),
      Triple(bnode("a"), URI("http://www.w3.org/2002/07/owl#intersectionOf"),     bnode("d")) ,
      Triple(bnode("a"), URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),  URI("http://www.w3.org/2002/07/owl#Class")),
      Triple(bnode("c"), URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),  URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")),
      Triple(bnode("b"), URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),  URI("http://www.w3.org/2002/07/owl#Class")),
      Triple(bnode("b"), URI("http://www.w3.org/2002/07/owl#complementOf"),       URI("http://example.com/owl/families/Parent")),
      Triple(bnode("d"), URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"), URI("http://example.com/owl/families/Person")),
      Triple(bnode("c"), URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"), bnode("b")),
      Triple(bnode("d"), URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),  bnode("c"))
    )

    (g         isIsomorphicWith expectedGraph) shouldEqual true
    (fg.graph  isIsomorphicWith expectedGraph) shouldEqual true
    (sg.graph  isIsomorphicWith expectedGraph) shouldEqual true
    (sg2.graph isIsomorphicWith expectedGraph) shouldEqual true
  }

  "Diesel/Owl must create the owl primer ontology" in {

    val families: Rdf#Graph = Seq(
      f.hasSpouse   -- rdf.typ ->- owl.SymmetricProperty ,
      f.hasRelative -- rdf.typ ->- owl.ReflexiveProperty ,
      f.parentOf    -- rdf.typ ->- owl.IrreflexiveProperty ,
      f.hasAncestor -- rdf.typ ->- owl.TransitiveProperty ,

      f.hasHusband  -- rdf.typ ->- owl.FunctionalProperty
                    -- rdf.typ ->- owl.InverseFunctionalProperty,

      f.hasWife -- rdf.typ            ->- owl.ObjectProperty
                -- rdfs.domain        ->- f.Man
                -- rdfs.range         ->- f.Woman
                -- rdfs.subPropertyOf ->- f.hasSpouse,

      f.hasSon    -- owl.propertyDisjointWith ->- f.hasDaughter,
      f.hasFather -- rdfs.subPropertyOf       ->- f.hasParent,

      f.hasParent -- owl.inverseOf            ->- f.hasChild,
      f.hasParent -- owl.propertyDisjointWith ->- f.hasSpouse,

      f.hasGrandparent -- owl.propertyChainAxiom ->- ( f.hasParent, f.hasParent ),
      f.hasUncle       -- owl.propertyChainAxiom ->- ( f.hasFather, f.hasBrother ),

      f.hasAge -- owl.equivalentProperty ->- otherOnt("age")
               -- rdf.typ                ->- owl.DatatypeProperty
               -- rdf.typ                ->- owl.FunctionalProperty
               -- rdfs.domain            ->- f.Person
               -- rdfs.range             ->- xsd.nonNegativeInteger,

      f.hasChild -- owl.equivalentProperty ->- otherOnt("child")
                 -- rdf.typ                ->- owl.AsymmetricProperty,

      f.Woman -- rdfs.subClassOf ->- f.Person,


      f.Mother --   rdfs.subClassOf ->- f.Woman
               --  owl.equivalentClass ->- (bnode
        -- rdf.typ            ->-  owl.Class
        -- owl.intersectionOf ->- ( f.Woman,  f.Parent )),

      f.hasSSN -- rdf.typ ->- owl.DatatypeProperty,

      f.Person -- rdf.typ             ->- owl.Class
        -- rdfs.comment        ->- "Represents the set of all people."
        -- owl.equivalentClass ->- f.Human
        -- owl.hasKey          ->- f.hasSSN,

      f.Parent -- owl.equivalentClass ->- (bnode
        -- rdf.typ     ->- owl.Class
        -- owl.unionOf ->-(f.Mother, f.Father)),

      f.Parent -- owl.equivalentClass ->- (bnode
        -- rdf.typ            ->- owl.Restriction
        -- owl.onProperty     ->- f.hasChild
        -- owl.someValuesFrom ->- f.Person ),

      f.Grandfather -- rdfs.subClassOf  ->- (bnode
        -- rdf.typ            ->- owl.Class
        -- owl.intersectionOf ->- ( f.Man,  f.Parent )),

      f.HappyPerson -- owl.equivalentClass  ->- (bnode
        -- rdf.typ ->-      owl.Class
        -- owl.intersectionOf ->- ( bnode -- rdf.typ            ->- owl.Restriction
                                          -- owl.onProperty     ->- f.hasChild
                                          -- owl.allValuesFrom  ->- f.HappyPerson,
                                    bnode -- rdf.typ            ->- owl.Restriction
                                          -- owl.onProperty     ->- f.hasChild
                                          -- owl.someValuesFrom ->- f.HappyPerson)),

      f.JohnsChildren -- owl.equivalentClass ->- ( bnode
        -- rdf.typ        ->- owl.Restriction
        -- owl.onProperty ->- f.hasParent
        -- owl.hasValue   ->- f.John),

      f.NarcisticPerson -- owl.equivalentClass ->- ( bnode
        -- rdf.typ   ->-     owl.Restriction
        -- owl.onProperty ->- f.loves
        -- owl.hasSelf   ->-  true ),

      f.MyBirthdayGuests -- owl.equivalentClass ->- ( bnode
        -- rdf.typ ->-  owl.Class
        -- owl.oneOf ->- ( f.Bill, f.John, f.Mary )),

      f.Orphan --  owl.equivalentClass ->- ( bnode
        -- rdf.typ ->-           owl.Restriction
        -- owl.onProperty    ->- (bnode -- owl.inverseOf ->-  f.hasChild)
        -- owl.allValuesFrom ->- f.Dead),

      f.Teenager -- rdfs.subClassOf ->- ( bnode
        -- rdf.typ            ->- owl.Restriction
        -- owl.onProperty     ->- f.hasAge
        -- owl.someValuesFrom ->- ( bnode
          -- rdf.typ              ->- rdfs.Datatype
          -- owl.onDatatype       ->- xsd.integer
          -- owl.withRestrictions ->- ( bnode -- xsd.minExclusive ->- 12,
                                        bnode -- xsd.maxInclusive ->- 19))),

      f.Man -- rdfs.subClassOf       ->- f.Person,
      bnode -- rdf.typ               ->- owl.Axiom
            -- owl.annotatedSource   ->- f.Man
            -- owl.annotatedProperty ->- rdfs.subClassOf
            -- owl.annotatedTarget   ->- f.Person
            -- rdfs.comment          ->- "States that every man is a person.",

      f.Adult -- owl.equivalentClass ->-otherOnt("Grownup"),

      f.Father -- rdfs.subClassOf  ->- ( bnode
        -- rdf.typ            ->- owl.Class
        -- owl.intersectionOf ->- ( f.Man,  f.Parent )),

      f.ChildlessPerson -- owl.equivalentClass ->- ( bnode
        -- rdf.typ            ->- owl.Class
        -- owl.intersectionOf ->- ( f.Person.toPG, bnode -- owl.complementOf ->- f.Parent)),

      f.ChildlessPerson -- rdfs.subClassOf ->- ( bnode
        -- rdf.typ            ->- owl.Class
        -- owl.intersectionOf ->- ( f.Person.toPG,
                                    bnode -- owl.complementOf ->- ( bnode
                                      -- rdf.typ            ->- owl.Restriction
                                      -- owl.onProperty     ->- (bnode -- owl.inverseOf ->- f.hasParent)
                                      -- owl.someValuesFrom ->- owl.Thing))),

      bnode -- rdf.typ            ->- owl.Class
            -- owl.intersectionOf ->- ( bnode -- rdf.typ   ->- owl.Class
                                              -- owl.oneOf ->- ( f.Mary,  f.Bill,  f.Meg ),
                                      f.Female.toPG)
            -- rdfs.subClassOf ->- ( bnode
              -- rdf.typ            ->- owl.Class
              -- owl.intersectionOf ->- ( f.Parent.toPG,
                                          bnode -- rdf.typ            ->- owl.Restriction
                                                -- owl.maxCardinality ->- 1 //^^xsdf.nonNegativeInteger ;
                                                -- owl.onProperty     ->- f.hasChild,
                                          bnode -- rdf.typ            ->- owl.Restriction
                                                -- owl.onProperty     ->- f.hasChild
                                                -- owl.allValuesFrom  ->- f.Female)),

      bnode -- rdf.typ     ->- owl.AllDisjointClasses
            -- owl.members ->- ( f.Mother,  f.Father,  f.YoungChild ),

      bnode -- rdf.typ     ->- owl.AllDisjointClasses
            -- owl.members ->- ( f.Woman,  f.Man ),

      f.personAge --  owl.equivalentClass ->- ( bnode
        -- rdf.typ              ->- rdfs.Datatype
        -- owl.onDatatype       ->- xsd.integer
        -- owl.withRestrictions ->- ( bnode -- xsd.minInclusive ->- 0,
                                      bnode -- xsd.maxInclusive ->- 150)),

      f.majorAge -- owl.equivalentClass ->- ( bnode
        -- rdf.typ ->-  rdfs.Datatype
        -- owl.intersectionOf ->- (
             f.personAge.toPG,
             bnode -- rdf.typ                  ->- rdfs.Datatype
                   -- owl.datatypeComplementOf ->- f.minorAge)),

      f.toddlerAge --  owl.equivalentClass ->- ( bnode
        -- rdf.typ   ->- rdfs.Datatype
        -- owl.oneOf ->- ( 1, 2)),

      f.Mary -- rdf.typ    ->- f.Person
             -- rdf.typ    ->- f.Woman
             -- owl.sameAs ->- otherOnt("MaryBrown"),

      f.James -- owl.sameAs ->- f.Jim,

      f.Jack -- rdf.typ ->- ( bnode
        -- rdf.typ            ->- owl.Class
        -- owl.intersectionOf ->- ( f.Person.toPG,
                                    bnode -- rdf.typ ->-          owl.Class
                                          -- owl.complementOf ->- f.Parent )),


      f.John -- owl.sameAs        ->- otherOnt("JohnBrown"),
      f.John -- rdf.typ           ->- owl.NamedIndividual,
      f.John -- rdf.typ           ->- f.Father,
      f.John -- f.hasWife         ->- f.Mary,
      f.John -- owl.differentFrom ->- f.Bill,
      f.John -- f.hasAge          ->- 51,

      f.John -- rdf.typ ->- ( bnode
        -- rdf.typ                     ->- owl.Restriction
        -- owl.maxQualifiedCardinality ->- 4
        -- owl.onProperty              ->-  f.hasChild
        -- owl.onClass                 ->-  f.Parent),

      f.John --  rdf.typ ->- ( bnode
        -- rdf.typ                      ->- owl.Restriction
        -- owl.minQualifiedCardinality  ->- 2
        -- owl.onProperty               ->- f.hasChild
        -- owl.onClass                  ->- f.Parent),

      f.John -- rdf.typ ->- ( bnode
        -- rdf.typ                   ->- owl.Restriction
        -- owl.qualifiedCardinality  ->- 3
        -- owl.onProperty            ->- f.hasChild
        -- owl.onClass               ->- f.Parent),

    f.John -- rdf.typ ->- ( bnode
      --rdf.typ         ->- owl.Restriction
      --owl.cardinality ->- 5
      --owl.onProperty  ->- f.hasChild),

    f.Father -- rdf.typ ->- f.SocialRole,

    bnode -- rdf.typ                ->- owl.NegativePropertyAssertion
          -- owl.sourceIndividual   ->- f.Bill
          -- owl.assertionProperty  ->- f.hasWife
          -- owl.targetIndividual   ->- f.Mary,

    bnode -- rdf.typ               ->- owl.NegativePropertyAssertion
          -- owl.sourceIndividual  ->- f.Bill
          -- owl.assertionProperty ->- f.hasDaughter
          -- owl.targetIndividual  ->- f.Susan,

    bnode --  rdf.typ               ->- owl.NegativePropertyAssertion
          -- owl.sourceIndividual   ->- f.Jack
          -- owl.assertionProperty  ->- f.hasAge
          -- owl.targetValue        ->- 53

    )
   //(g  isIsomorphicWith g2) shouldEqual true
  }

  "Diesel/Owl must compile" in {
    val g: Rdf#Graph = Seq(
      f("John") a owl.NamedIndividual,
      f("Mary") a owl.NamedIndividual,
      f("Jim") a owl.NamedIndividual,
      f("James") a owl.NamedIndividual,
      f("Jack") a owl.NamedIndividual,
      f("Bill") a owl.NamedIndividual,
      f("Susan") a owl.NamedIndividual,
      (f("Person") a owl.Class)
        -- rdfs.comment ->- "Represents the set of all people."
        -- owl.equivalentClass ->- f("Human")
    )

    val g2: Rdf#Graph = Seq(
      f.John a owl.NamedIndividual,
      f.Mary a owl.NamedIndividual,
      f.Jim a owl.NamedIndividual,
      f.James a owl.NamedIndividual,
      f.Jack a owl.NamedIndividual,
      f.Bill a owl.NamedIndividual,
      f.Susan a owl.NamedIndividual,
      (f.Person a owl.Class)
        -- rdfs.comment ->- "Represents the set of all people."

    )

    val person: Rdf#Graph = Seq(
      f.hasSSN -- rdf.typ ->- owl.DatatypeProperty,

      f.Person -- rdf.typ ->- owl.Class
        -- rdfs.comment ->- "Represents the set of all people."
        -- owl.equivalentClass ->- f.Human
        -- owl.hasKey ->- f.hasSSN,

      f.Parent -- owl.equivalentClass ->- (bnode
        -- rdf.typ ->- owl.Class
        -- owl.unionOf ->-(f.John, f.Bill)
        )

    )
  }
}
