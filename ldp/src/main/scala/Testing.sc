
import org.w3.banana.plantain.PlantainOps._
import util.parsing.combinator.JavaTokenParsers
import org.w3.banana._
import org.w3.banana.ldp._

implicit val ops = org.w3.banana.plantain.PlantainOps






val lhp = new LinkHeaderParser






lhp.parse("""</>; rev=http://xmlns.com/foaf/0.1/knows  ; anchor="#me"""")























































