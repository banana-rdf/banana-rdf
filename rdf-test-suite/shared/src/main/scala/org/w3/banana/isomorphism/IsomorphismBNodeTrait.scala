package org.w3.banana.isomorphism

import org.w3.banana._


trait IsomorphismBNodeTrait[Rdf <: RDF] {

  implicit val ops: RDFOps[Rdf]

  import ops._

  val foaf = FOAFPrefix[Rdf]
  val rdf = RDFPrefix[Rdf]

  val hjs = URI("http://bblfish.net/people/henry/card#me")
  val timbl = URI("http://www.w3.org/People/Berners-Lee/card#i")
  def alex(i: Int) = BNode("alex" + i)
  def antonio(i: Int) = BNode("antonio" + i)

  def groundedGraph = (
    hjs
      -- foaf.knows ->- timbl
      -- foaf.name ->- "Henry Story"
  ).graph

  def bn(bnprefix: String)(i: Int) = BNode(bnprefix + i)

  def list(size: Int, bnPrefix: String) = {
    val p = bnPrefix
    (1 to size).foldRight(
      Graph(Triple(bn(p)(0), rdf.first, Literal("0", xsd.integer)),
        Triple(bn(p)(0), rdf.rest, rdf.nil))) {
        case (i, g) =>
          g union (
            bn(p)(i) -- rdf.first ->- i
              -- rdf.rest ->- bn(p)(i - 1)
          ).graph

      }
  }

  //  val bnodeGraph = (
  //      toPointedGraphW[Rdf](URI("#me"))
  //        -- foaf.knows ->- toPointedGraphW[Rdf](bnode("alex"))
  //    ).graph union (
  //      toPointedGraphW[Rdf](bnode("alex"))
  //        -- foaf.name ->- "Alexandre Bertails"
  //    ).graph

  def bnAlexRel1Graph(i: Int = 1) = Graph(
    Triple(alex(i), foaf.homepage, URI("http://bertails.org/")))

  def bnAlexRel2Graph(i: Int = 1) = Graph(
    Triple(hjs, foaf.knows, alex(i)),
    Triple(alex(i), foaf.name, "Alexandre Bertails".toNode))

  def bnAntonioRel1Graph(i: Int = 1) = Graph(Triple(antonio(i), foaf("homepage"), URI("https://github.com/antoniogarrote/")))

  def bnAntonioRel2Graph(i: Int = 1) = Graph(
    Triple(hjs, foaf.knows, antonio(i)),
    Triple(antonio(i), foaf.name, "Antonio Garrote".toNode))

  def xbn(i: Int) = BNode("x" + i)

  def bnKnowsBN(i: Int, j: Int) = Graph(
    Triple(xbn(i), foaf.knows, xbn(j)))

  def symmetricGraph(i: Int, j: Int) = bnKnowsBN(i, j) union bnKnowsBN(j, i)

  def owlSameAs(node1: Rdf#Node, node2: Rdf#Node) =
    Graph(Triple(node1, URI("http://www.w3.org/2002/07/owl#sameAs"), node2))

}
