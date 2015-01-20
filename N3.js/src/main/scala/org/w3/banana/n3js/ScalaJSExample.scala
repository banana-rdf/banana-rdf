package example

import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom

@JSExport
object ScalaJSExample {

  @JSExport
  def main(canvas: dom.HTMLCanvasElement): Unit = {

    import scala.scalajs.js
    import org.w3.banana.n3js._

    val parser = N3.Parser()

    var triples: Vector[Triple] = Vector.empty

    parser.parse(
      (error: js.Any, triple: js.UndefOr[Triple], prefixes: js.UndefOr[js.Any]) => {
        triple.foreach { (t: Triple) => triples :+= t }
        prefixes.foreach { p => println("prefixes: "+p) }
      }
    )

    parser.addChunk("@prefix c: <http://example.org/cartoons#>.\n")
    parser.addChunk("c:Tom a ")
    parser.addChunk("c:Cat. c:Jerry a")
    parser.end()

    println(N3.Util.isIRI("http://example.org/cartoons#Mickey"))

  }

}
