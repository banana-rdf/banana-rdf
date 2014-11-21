package org.w3.banana.ldpatch

object Main extends App {

  def extractLiterals2(input: String, delim: Char): List[(Int, Int)] = {
    def consumeDelims(r: Int): Int = if (input.charAt(r) == delim) consumeDelims(r + 1) else r
    def skipNonDelims(r: Int): Option[Int] = input.charAt(r) match {
      case ' '     => None
      case `delim` => Some(r)
      case _       => util.Try(skipNonDelims(r + 1)).toOption.flatten
    }
    def loop(l: Int, acc: List[(Int, Int)]): List[(Int, Int)] = {
      if (l >= input.size - 1) {
        acc
      } else if (input.charAt(l) == delim) {
        if (input.charAt(l + 1) == delim) {
          loop(consumeDelims(l), acc)
        } else {
          skipNonDelims(l + 1) match {
            case Some(next) =>
              loop(next + 1, (l + 1, next) :: acc)
            case None =>
              loop(l + 1, acc)
          }
        }
      } else {
        loop(l + 1, acc)
      }
    }
    loop(0, List.empty).filter { case (l, r) => ! input.substring(l, r).contains(' ') }
  }

  implicit class StringW(val s: String) extends AnyVal {

    def replace2(l: Int, r: Int, f: String => String): String = {
      s.substring(0, l) ++ f(s.substring(l, r)) + s.substring(r)
    }

  }

  // the grammar rules
  val input = """
ldpatch ::= prologue statement*
prologue ::= prefixID*
statement ::= bind | add | delete | updateList
bind ::= ("Bind" | "B") Var value path? "."
add ::= ("Add" | "A") "{" graph "}" "."
delete ::= ("Delete" | "D") "{" graph "}" "."
cut ::= ("Cut" | "C") varOrIRI "."
updateList ::= ("UpdateList" | "UL") varOrIRI predicate slice collection "."
value ::= iri | literal | Var
path ::= ('/'? step | constraint )? ( '/' step | constraint )*
step ::= '^' iri | iri | INDEX
constraint ::= '[' path ( '=' value )? ']' | '!'
slice ::= INDEX? '..' INDEX?
INDEX ::= [0-9]+
[143s] VAR1 ::= '?' VARNAME
[166s] VARNAME ::= ( PN_CHARS_U | [0-9] ) ( PN_CHARS_U | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040] )*
[4t] prefixID ::= "@prefix" PNAME_NS IRIREF "."
graph ::= triples ( '.' triples )* '.'?
[6t] triples ::= subject predicateObjectList | blankNodePropertyList predicateObjectList?
[7t] predicateObjectList ::= verb objectList (';' (verb objectList)?)*
[8t] objectList ::= object (',' object)*
[9t] verb ::= predicate | 'a'
[10t*] subject ::= iri | BlankNode | collection | VAR1
[11t] predicate ::= iri
[12t*] object ::= iri | BlankNode | collection | blankNodePropertyList | literal | VAR1
[13t] literal ::= RDFLiteral | NumericLiteral | BooleanLiteral
[14t] blankNodePropertyList ::= '[' predicateObjectList ']'
[15t] collection ::= '(' object* ')'
[16t] NumericLiteral ::= INTEGER | DECIMAL | DOUBLE
[128s] RDFLiteral ::= String (LANGTAG | '^^' iri)?
[133s] BooleanLiteral ::= 'true' | 'false'
[17] String ::= STRING_LITERAL_QUOTE | STRING_LITERAL_SINGLE_QUOTE | STRING_LITERAL_LONG_SINGLE_QUOTE | STRING_LITERAL_LONG_QUOTE
[135s] iri ::= IRIREF | PrefixedName
[143s] varOrIRI ::= iri | VAR1
[136s] PrefixedName ::= PNAME_LN | PNAME_NS
[137s] BlankNode ::= BLANK_NODE_LABEL | ANON
[18] IRIREF ::= '<' ([^#x00-#x20<>"{}|^`\] | UCHAR)* '>' /* #x00=NULL #01-#x1F=control codes #x20=space */
[139s] PNAME_NS ::= PN_PREFIX? ':'
[140s] PNAME_LN ::= PNAME_NS PN_LOCAL
[141s] BLANK_NODE_LABEL ::= '_:' (PN_CHARS_U | [0-9]) ((PN_CHARS | '.')* PN_CHARS)?
[144s] LANGTAG ::= '@' [a-zA-Z]+ ('-' [a-zA-Z0-9]+)*
[19] INTEGER ::= [+-]? [0-9]+
[20] DECIMAL ::= [+-]? [0-9]* '.' [0-9]+
[21] DOUBLE ::= [+-]? ([0-9]+ '.' [0-9]* EXPONENT | '.' [0-9]+ EXPONENT | [0-9]+ EXPONENT)
[154s] EXPONENT ::= [eE] [+-]? [0-9]+
[22] STRING_LITERAL_QUOTE ::= '"' ([^#x22#x5C#xA#xD] | ECHAR | UCHAR)* '"'      /* #x22=" #x5C=\ #xA=new line #xD=carriage return */
[23] STRING_LITERAL_SINGLE_QUOTE ::= "'" ([^#x27#x5C#xA#xD] | ECHAR | UCHAR)* "'"      /* #x27=' #x5C=\ #xA=new line #xD=carriage return */
[24] STRING_LITERAL_LONG_SINGLE_QUOTE ::= "'''" (("'" | "''")? ([^'\] | ECHAR | UCHAR))* "'''"
[25] STRING_LITERAL_LONG_QUOTE ::= '___' (('"' | '""')? ([^"\] | ECHAR | UCHAR))* '___'
[26] UCHAR ::= '\\u' HEX HEX HEX HEX | '\\U' HEX HEX HEX HEX HEX HEX HEX HEX
[159s] ECHAR ::= '\' [tbnrf"'\]
[161s] WS ::= #x20 | #x9 | #xD | #xA
[162s] ANON ::= '[' WS* ']'
[163s] PN_CHARS_BASE ::= [A-Z] | [a-z] | [#x00C0-#x00D6] | [#x00D8-#x00F6] | [#x00F8-#x02FF] | [#x0370-#x037D] | [#x037F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
[164s] PN_CHARS_U ::= PN_CHARS_BASE | '_'
[166s] PN_CHARS ::= PN_CHARS_U | '-' | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040]
[167s] PN_PREFIX ::= PN_CHARS_BASE ((PN_CHARS | '.')* PN_CHARS)?
[168s] PN_LOCAL ::= (PN_CHARS_U | ':' | [0-9] | PLX) ((PN_CHARS | '.' | ':' | PLX)* (PN_CHARS | ':' | PLX))?
[169s] PLX ::= PERCENT | PN_LOCAL_ESC
[170s] PERCENT ::= '%' HEX HEX
[171s] HEX ::= [0-9] | [A-F] | [a-f]
[172s] PN_LOCAL_ESC ::= '\' ('_' | '~' | '.' | '-' | '!' | '$' | '&' | "'" | '(' | ')' | '*' | '+' | ',' | ';' | '=' | '/' | '?' | '#' | '@' | '%')
""".replaceAll("___", "\"\"\"")

  case class Rule(opt: Option[String], lhs: String, rhs: String)

  val rule = """^(\[\w+\*?\])? *([^:]+) +::= +(.*)$""".r

  val rules = input.split("\n").to[List].filter(_.nonEmpty).map {
    case rule(null, l, r) => Rule(None, l, r)
    case rule(opt, l, r)  => Rule(Some(opt), l, r)
  }

  // https://stackoverflow.com/questions/1240275/how-to-negate-specific-word-in-regex
  // (?<!>) means: go back one character, do *not* find a '>'
  val defs = rules.map(_.lhs).sortWith { case (s1, s2) => s1 > s2 }.map(rule => s"(?<![>-])($rule)".r)

  def loop(rules: List[Rule], counter: Int): List[(String, String, String)] = rules match {
    case Rule(Some(s), l, r) :: rest => (s, l, r) :: loop(rest, counter)
    case Rule(None, l, r) :: rest    => (s"[$counter]", l, r) :: loop(rest, counter + 1)
    case Nil                         => Nil
  }

  val prods = loop(rules, 1).map { case (s, l, r) =>

    val literals = extractLiterals2(r, '\'') ++ extractLiterals2(r, '"') sortWith { case ((l1, _), (l2, _)) => l1 > l2 }

    val td1 = literals.foldLeft(r) { case (acc, (l, r)) =>
      acc.replace2(l, r, lit => s"""<code class="grammar-literal">$lit</code>""")
    }

    val td = defs.foldLeft(td1){ case (acc, d) => d.replaceAllIn(acc, m => {
      val ref = m.group(1)
      s"""<a href="#grammar-production-$ref">$ref</a>"""
    })}

    val tr = s"""
<tr id="grammar-production-$l">
    <td>$s</td>
    <td><a href="#grammar-production-$l">$l</a></td>
    <td>::=</td>
    <td>$td</td>
</tr>
""".trim
    tr
  }

  val table = s"""
<table class="grammar">
  <tbody class="grammar-productions">
${prods.mkString("\n")}
  </tbody>
</table>
""".trim

//  println(table)
  import java.io._
  val w = new FileWriter("/tmp/output.txt")
  w.write(table.toString)
  w.flush()

//  val i = """'___' (('"' | '""')? ([^"\] | ECHAR | UCHAR))* '___'""".replaceAll("___", "\"\"\"")
//  val i = """bind ::= ("Bind" | "B") Var value path? ".""""
//  val literals = extractLiterals2(i, '"')
//  println(literals)
//  val foo = literals.foldLeft(i) { case (acc, (l, r)) =>
//    acc.replace2(l, r, lit => s"""<code class="grammar-literal">$lit</code>""")
//  }
//  println(foo)

}
