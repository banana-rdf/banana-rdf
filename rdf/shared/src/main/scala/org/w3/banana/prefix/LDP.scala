package org.w3.banana.prefix
import org.w3.banana.{RDF, Ops, PrefixBuilder}



object LDP:
  def apply[T <: RDF](using Ops[T]) = new LDP[T]

class LDP[R <: RDF](using Ops[R])
  extends PrefixBuilder[R](
	"ldp",
	"http://www.w3.org/ns/ldp#"
):
  val AggregateContainer = apply("AggregateContainer")
  val CompositeContainer = apply("CompositeContainer")
  val Container = apply("Container")
  val Page = apply("Page")
  val Resource = apply("Resource")
  val containerSortPredicates = apply("containerSortPredicates")
  val membershipPredicate = apply("membershipPredicate")
  val membershipSubject = apply("membershipSubject")
  val nextPage = apply("nextPage")
  val created = apply("created")
  val pageOf = apply("pageOf")
end LDP
