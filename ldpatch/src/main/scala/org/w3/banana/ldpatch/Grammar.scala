package org.w3.banana.ldpatch

import org.w3.banana._
import scala.util.Try
import org.w3.banana.ldpatch.{ model => m }

trait Grammar[Rdf <: RDF] {

  implicit val ops: RDFOps[Rdf]

  import ops._

  object grammar {

    import org.parboiled2._
    import CharPredicate._

    def between(low: Char, high: Char): CharPredicate = CharPredicate.from(c => c >= low && c <= high)

    class PEGPatchParser(
      val input: ParserInput,
      baseURI: Rdf#URI,
      var prefixes: Map[String, Rdf#URI] = Map.empty,
      var bnodeMap: Map[String, Rdf#BNode] = Map.empty
    ) extends Parser with StringBuilding {


      /** use this method to maintain the map from bnodes label to actual
        * unique labels
        */
      def makeBNode(label: String): Rdf#BNode = {
        this.bnodeMap.getOrElse(label, {
          val bnode = BNode()
          this.bnodeMap += (label -> bnode)
          bnode
        })
      }
      
      // token separators

      def WS0: Rule0 = rule { zeroOrMore(WS) }

      def WS1: Rule0 = rule { oneOrMore(WS) }

      // LDPatch ::= Prologue Statement*
      def LDPatch: Rule1[m.LDPatch[Rdf]] = rule {
        WS0 ~ Prologue ~> (prefixes => this.prefixes = prefixes) ~ WS1 ~ zeroOrMore(Statement).separatedBy(WS1) ~ WS0 ~ EOI ~> ((statements: Seq[m.Statement[Rdf]]) => m.LDPatch(statements))
      }

      // Prologue ::= prefixID*
      def Prologue: Rule1[Map[String, Rdf#URI]] = rule { zeroOrMore(prefixID).separatedBy(WS1) ~> ((prefixes: Seq[(String, Rdf#URI)]) => push(this.prefixes ++ prefixes)) }

      // Statement ::= Bind | Add | Delete | UpdateList
      def Statement: Rule1[m.Statement[Rdf]] = rule (
        Bind | Add | Delete | UpdateList
      )

      // Bind ::= ("Bind" | "B") Var Value Path? "."
      def Bind: Rule1[m.Bind[Rdf]] = rule (
        ("Bind" | 'B') ~ WS1 ~ Var ~ WS1 ~ Value ~ optional(WS0 ~ Path) ~ WS0 ~ '.' ~> ((varr: m.Var, value: m.VarOrConcrete[Rdf], pathOpt: Option[m.Path[Rdf]]) => m.Bind(varr, value, pathOpt.getOrElse(m.Path(Seq.empty))))
      )

      // Add ::= ("Add" | "A") Subject Predicate ( Object | List ) "."
      def Add: Rule1[m.Statement[Rdf]] = rule (
        ("Add" | 'A') ~ WS1 ~ Subject ~ WS1 ~ Predicate ~ WS1 ~ (
            List ~> (Right(_))
          | Object ~> (Left(_))
        ) ~ WS0 ~ '.' ~> ((s: m.VarOrConcrete[Rdf], p: Rdf#URI, objectOrList: Either[m.VarOrConcrete[Rdf], Seq[m.VarOrConcrete[Rdf]]]) => objectOrList match {
          case Left(o)     => m.Add(s, p, o)
          case Right(list) => m.AddList(s, p, list)
        })
      )

      // Delete ::= ("Delete" | "D") Subject Predicate Object "."
      def Delete: Rule1[m.Delete[Rdf]] = rule (
        ("Delete" | 'D') ~ WS1 ~ Subject ~ WS1 ~ Predicate ~ WS1 ~ Object ~ WS0 ~ '.' ~> ((s: m.VarOrConcrete[Rdf], p: Rdf#URI, o: m.VarOrConcrete[Rdf]) => m.Delete(s, p, o))
      )

      // UpdateList ::= ("UpdateList" | "UL") Subject Predicate Slice List "."
      def UpdateList: Rule1[m.UpdateList[Rdf]] = rule (
        ("UpdateList" | "UL") ~ WS1 ~ Subject ~ WS1 ~ Predicate ~ WS1 ~ Slice ~ WS1 ~ List ~ WS0 ~ '.' ~> ((s: m.VarOrConcrete[Rdf], p: Rdf#URI, slice: m.Slice, list: Seq[m.VarOrConcrete[Rdf]]) => m.UpdateList(s, p, slice, list))
      )

      // Subject ::= iri | BlankNode | Var
      def Subject: Rule1[m.VarOrConcrete[Rdf]] = rule (
          iri ~> (m.Concrete(_))
        | BlankNode ~> (m.Concrete(_))
        | Var
      )

      // Predicate ::= iri
      def Predicate: Rule1[Rdf#URI] = rule (
        iri
      )

      // Object ::= iri | BlankNode | literal | Var
      def Object: Rule1[m.VarOrConcrete[Rdf]] = rule (
          iri ~> (m.Concrete(_))
        | BlankNode ~> (m.Concrete(_))
        | literal ~> (m.Concrete(_))
        | Var
      )

      // Value ::= iri | literal | Var
      def Value: Rule1[m.VarOrConcrete[Rdf]] = rule (
          iri ~> (m.Concrete(_))
        | literal ~> (m.Concrete(_))
        | Var
      )

      // List ::= '(' Object* ')'
      def List: Rule1[Seq[m.VarOrConcrete[Rdf]]] = rule (
        '(' ~ WS0 ~ zeroOrMore(Object).separatedBy(WS1) ~ WS0 ~ ')'
      )

      // Path ::= ( Step | Constraint )*
      def Path: Rule1[m.Path[Rdf]] = rule (
        zeroOrMore(Step | Constraint).separatedBy(WS0) ~> ((pathElems: Seq[m.PathElement[Rdf]]) => m.Path(pathElems))
      )

      // Step ::= '/' ( '-' iri | iri | INDEX )
      def Step: Rule1[m.Step[Rdf]] = rule (
        '/' ~ (
            '-' ~ iri ~> ((uri: Rdf#URI) => m.StepBackward(uri))
          | iri ~> ((uri: Rdf#URI) => m.StepForward(uri))
          | INDEX ~> (m.StepAt(_: Int))
        )
      )

      // Constraint ::= '[' Path ( '=' Value )? ']' | '!'
      def Constraint: Rule1[m.Constraint[Rdf]] = rule (
          '[' ~ WS0 ~ Path ~ optional(WS0 ~ '=' ~ WS0 ~ Value) ~ WS0 ~ ']' ~> ((path: m.Path[Rdf], valueOpt: Option[m.VarOrConcrete[Rdf]]) => m.Filter(path, valueOpt))
        | '!' ~ push(m.UnicityConstraint)
      )

      // Slice ::= INDEX? '>' INDEX?
      def Slice: Rule1[m.Slice] = rule (
        optional(INDEX) ~ '>' ~ optional(INDEX) ~> ((leftOpt: Option[Int], rightOpt: Option[Int]) => (leftOpt, rightOpt) match {
          case (Some(left), Some(right)) => m.Range(left, right)
          case (Some(index), None)       => m.EverythingAfter(index)
          case (None, Some(index))       => m.Range(0, index)
          case (None, None)              => m.End
        })
      )

      // INDEX ::= [0-9]+
      def INDEX: Rule1[Int] = rule (
        capture(oneOrMore(Digit)) ~> ((s: String) => s.toInt)
      )

      // copied from SPARQL

      // the VAR1 from SPARQL:   Var ::= '?' VARNAME
      def Var: Rule1[m.Var] = rule (
        '?' ~ VARNAME
      )

      // VARNAME ::= ( PN_CHARS_U | [0-9] ) ( PN_CHARS_U | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040] )*
      def VARNAME: Rule1[m.Var] = rule (
        clearSB() ~ (PN_CHARS_U | CharPredicate.Digit ~ appendSB()) ~ zeroOrMore(
            PN_CHARS_U
          | (CharPredicate.Digit ++ CharPredicate('\u00B7') ++ between('\u0300', '\u036F') ++ between('\u203F', '\u2040')) ~ appendSB()
        ) ~ push(m.Var(sb.toString))
      )

      // copied from Turtle

      // prefixID ::= "@prefix" PNAME_NS IRIREF "."
      def prefixID: Rule1[(String, Rdf#URI)] = rule {
        "@prefix" ~ WS1 ~ PNAME_NS ~ WS0 ~ IRIREF ~ WS0 ~ '.' ~> ((qname: String, iri: Rdf#URI) => (qname, iri))
      }
      
      // TODO: check if it's literal or Literal
      // literal ::= RDFLiteral | NumericLiteral | BooleanLiteral
      def literal: Rule1[Rdf#Literal] = rule (
        RDFLiteral | NumericLiteral | BooleanLiteral
      )

      // NumericLiteral ::= INTEGER | DECIMAL | DOUBLE
      def NumericLiteral: Rule1[Rdf#Literal] = rule (
          DOUBLE ~> ((lexicalForm: String) => Literal(lexicalForm, xsd.double))
        | DECIMAL ~> ((lexicalForm: String) => Literal(lexicalForm, xsd.decimal))
        | INTEGER ~> ((lexicalForm: String) => Literal(lexicalForm, xsd.integer))
      )

      // RDFLiteral ::= String (LANGTAG | '^^' iri)?
      def RDFLiteral: Rule1[Rdf#Literal] = rule (
        String ~ optional(LangOrIRI) ~> ((lexicalForm: String, opt: Option[Either[Rdf#Lang, Rdf#URI]]) => opt match {
          case None                  => Literal(lexicalForm)
          case Some(Left(langtag))   => Literal.tagged(lexicalForm, langtag)
          case Some(Right(datatype)) => Literal(lexicalForm, datatype)
        })
      )

      // just the (LANGTAG | '^^' iri) part
      def LangOrIRI: Rule1[Either[Rdf#Lang, Rdf#URI]] = rule (
          LANGTAG ~> ((lang: Rdf#Lang) => Left(lang))
        | "^^" ~ iri ~> ((datatype: Rdf#URI) => Right(datatype))
      )

      // BooleanLiteral ::= 'true' | 'false'
      def BooleanLiteral: Rule1[Rdf#Literal] = rule (
          "true" ~ push(xsd.`true`)
        | "false" ~ push(xsd.`false`)
      )

      // String ::= STRING_LITERAL_QUOTE | STRING_LITERAL_SINGLE_QUOTE | STRING_LITERAL_LONG_SINGLE_QUOTE | STRING_LITERAL_LONG_QUOTE
      def String: Rule1[String] = rule (
        STRING_LITERAL_QUOTE | STRING_LITERAL_SINGLE_QUOTE | STRING_LITERAL_LONG_SINGLE_QUOTE | STRING_LITERAL_LONG_QUOTE
      )

      // iri ::= IRIREF | PrefixedName
      def iri: Rule1[Rdf#URI] = rule (
        IRIREF | PrefixedName
      )

      // PrefixedName ::= PNAME_LN | PNAME_NS
      def PrefixedName: Rule1[Rdf#URI] = rule (
          PNAME_LN ~> ((prefix, localName) => URI(prefixes(prefix).getString + localName))
        | PNAME_NS ~> (prefix => URI(prefix))
      )

      // BlankNode ::= BLANK_NODE_LABEL | ANON
      def BlankNode: Rule1[Rdf#BNode] = rule (
        (BLANK_NODE_LABEL | ANON)
      )

      // IRIREF ::= '<' ([^#x00-#x20<>"{}|^`\] | UCHAR)* '>' /* #x00=NULL #01-#x1F=control codes #x20=space */
      def IRIREF: Rule1[Rdf#URI] = rule {
        '<' ~ clearSB() ~ zeroOrMore(IRIREF_CHAR) ~ '>' ~ push(baseURI.resolve(URI(sb.toString())))
      }
  
      // matches a Char in [^#x00-#x20<>"{}|^`\] or /uxxxx or /Uxxxxxxxx, and pushes it on the StringBuffer
      def IRIREF_CHAR: Rule0 = rule (
          UCHAR
        | (CharPredicate('\u0000' to '\u0020') ++ CharPredicate("<>\"{}|^`\\")).negated ~ appendSB()
      )

      // PNAME_NS ::= PN_PREFIX? ':'
      def PNAME_NS: Rule1[String] = rule {
        optional(PN_PREFIX) ~ ':' ~> ((prefixOpt: Option[String]) => push(prefixOpt.getOrElse("")))
      }

      // PNAME_LN ::= PNAME_NS PN_LOCAL
      def PNAME_LN: Rule2[String, String] = rule (
        PNAME_NS ~ PN_LOCAL
      )

      // BLANK_NODE_LABEL ::= '_:' (PN_CHARS_U | [0-9]) ((PN_CHARS | '.')* PN_CHARS)?
      def BLANK_NODE_LABEL: Rule1[Rdf#BNode] = rule (
        "_:" ~ clearSB() ~ BLANK_NODE_LABEL1 ~ optional(BLANK_NODE_LABEL2) ~ push(makeBNode(sb.toString()))
      )

      // PN_CHARS_U | [0-9]
      def BLANK_NODE_LABEL1: Rule0 = rule (
          PN_CHARS_U
        | Digit ~ appendSB()
      )

      // (PN_CHARS | '.')* PN_CHARS
      def BLANK_NODE_LABEL2: Rule0 = rule (
        oneOrMore(PN_CHARS) ~ test(lastChar != '.')
      )

      // LANGTAG ::= '@' [a-zA-Z]+ ('-' [a-zA-Z0-9]+)*
      def LANGTAG: Rule1[Rdf#Lang] = rule (
        '@' ~ capture(oneOrMore(CharPredicate.Alpha) ~ zeroOrMore('-' ~ oneOrMore(CharPredicate.AlphaNum))) ~> ((langString: String) => Lang(langString))
      )

      // INTEGER ::= [+-]? [0-9]+
      def INTEGER: Rule1[String] = rule (
        capture(optional(anyOf("+-")) ~ oneOrMore(CharPredicate.Digit))
      )

      // DECIMAL ::= [+-]? [0-9]* '.' [0-9]+
      def DECIMAL: Rule1[String] = rule (
        capture(optional(anyOf("+-")) ~ zeroOrMore(CharPredicate.Digit) ~ '.' ~ zeroOrMore(CharPredicate.Digit))
      )

      // DOUBLE ::= [+-]? ([0-9]+ '.' [0-9]* EXPONENT | '.' [0-9]+ EXPONENT | [0-9]+ EXPONENT)
      def DOUBLE: Rule1[String] = rule (
        capture(optional(anyOf("+-")) ~ (
            oneOrMore(CharPredicate.Digit) ~ '.' ~ zeroOrMore(CharPredicate.Digit) ~ EXPONENT
          | '.' ~ oneOrMore(CharPredicate.Digit) ~ EXPONENT
          | oneOrMore(CharPredicate.Digit) ~ EXPONENT
        ))
      )

      // EXPONENT ::= [eE] [+-]? [0-9]+
      def EXPONENT: Rule0 = rule (
        anyOf("eE") ~ optional(anyOf("+-")) ~ oneOrMore(CharPredicate.Digit)
      )

      // STRING_LITERAL_QUOTE ::= '"' ([^#x22#x5C#xA#xD] | ECHAR | UCHAR)* '"'      /* #x22=" #x5C=\ #xA=new line #xD=carriage return */
      def STRING_LITERAL_QUOTE: Rule1[String] = rule (
        '"' ~ clearSB() ~ zeroOrMore(ECHAR | UCHAR | noneOf("\"\\\n\r") ~ appendSB()) ~ '"' ~ push(sb.toString())
      )

      // STRING_LITERAL_SINGLE_QUOTE ::= "'" ([^#x27#x5C#xA#xD] | ECHAR | UCHAR)* "'"      /* #x27=' #x5C=\ #xA=new line #xD=carriage return */
      def STRING_LITERAL_SINGLE_QUOTE: Rule1[String] = rule (
        '\'' ~ clearSB() ~ zeroOrMore(ECHAR | UCHAR | noneOf("\"\\\n\r") ~ appendSB()) ~ '\'' ~ push(sb.toString())
      )

      // STRING_LITERAL_LONG_SINGLE_QUOTE ::= "'''" (("'" | "''")? ([^'\] | ECHAR | UCHAR))* "'''"
      def STRING_LITERAL_LONG_SINGLE_QUOTE: Rule1[String] = rule (
        "'''" ~ clearSB() ~ zeroOrMore(optional('\'' ~ appendSB() ~ optional('\'' ~ appendSB())) ~ (ECHAR | UCHAR | noneOf("'\\") ~ appendSB())) ~ "'''" ~ push(sb.toString())
      )

      // STRING_LITERAL_LONG_QUOTE ::= '"""' (('"' | '""')? ([^"\] | ECHAR | UCHAR))* '"""'
      def STRING_LITERAL_LONG_QUOTE: Rule1[String] = rule (
        "\"\"\"" ~ clearSB() ~ zeroOrMore(optional('"' ~ appendSB() ~ optional('"' ~ appendSB())) ~ (ECHAR | UCHAR | noneOf("\"\\") ~ appendSB())) ~ "\"\"\"" ~ push(sb.toString())
      )

      // UCHAR ::= '\\u' HEX HEX HEX HEX | '\\U' HEX HEX HEX HEX HEX HEX HEX HEX
      def UCHAR: Rule0 = rule {
        "\\u" ~ capture(HexDigit ~ HexDigit ~ HexDigit ~ HexDigit) ~> ((code: String) => appendSB(java.lang.Integer.parseInt(code, 16).asInstanceOf[Char]))
      }

      // ECHAR ::= '\' [tbnrf"'\]
      def ECHAR: Rule0 = rule (
        '\\' ~ (
            't'  ~ appendSB('\t')
          | 'b'  ~ appendSB('\b')
          | 'n'  ~ appendSB('\n')
          | 'r'  ~ appendSB('\r')
          | 'f'  ~ appendSB('\f')
          | '\'' ~ appendSB('\"')
          | '\'' ~ appendSB('\'')
          | '\\' ~ appendSB('\\')
        )
      )

      def WS: Rule0 = rule { anyOf(" \t\r\n") }

      // ANON ::= '[' WS* ']'
      def ANON: Rule1[Rdf#BNode] = rule (
        '[' ~ WS0 ~ ']' ~ push(BNode())
      )

      // PN_CHARS_BASE ::= [A-Z] | [a-z] | [#x00C0-#x00D6] | [#x00D8-#x00F6] | [#x00F8-#x02FF] | [#x0370-#x037D] | [#x037F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
      def PN_CHARS_BASE: Rule0 = rule (
          CharPredicate.Alpha ~ appendSB()
        | (between('\u00C0', '\u00D6') ++ between('\u00D8', '\u00F6') ++ between('\u00F8', '\u02FF') ++ between('\u0370', '\u037D') ++ between('\u037F', '\u1FFF') ++ between('\u200C', '\u200D') ++ between('\u2070', '\u218F') ++ between('\u2C00', '\u2FEF') ++ between('\u3001', '\uD7FF') ++ between('\uF900', '\uFDCF') ++ between('\uFDF0', '\uFFFD')) ~ appendSB()
      )

      // PN_CHARS_U ::= PN_CHARS_BASE | '_'
      def PN_CHARS_U: Rule0 = rule (
          PN_CHARS_BASE
        | '_' ~ appendSB()
      )

      // PN_CHARS ::= PN_CHARS_U | '-' | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040]
      def PN_CHARS: Rule0 = rule (
          PN_CHARS_U
        | (CharPredicate('-') ++ CharPredicate.Digit ++ CharPredicate('\u00B7') ++ between('\u0300', '\u036F') ++ between('\u203F', '\u2040')) ~ appendSB()
      )

      // PN_PREFIX ::= PN_CHARS_BASE ((PN_CHARS | '.')* PN_CHARS)?
      def PN_PREFIX: Rule1[String] = rule {
        clearSB() ~ PN_CHARS_BASE ~ optional(PN_PREFIX2) ~ push(sb.toString())
      }
  
      // (PN_CHARS | '.')* PN_CHARS
      // so basically PN_CHARS+ with no trailing '.'
      def PN_PREFIX2: Rule0 = rule {
  //      capture(oneOrMore(PN_CHARS)) ~> ((s: String) => test(s.charAt(s.length-1) != '.') ~ appendSB(s))
        oneOrMore(PN_CHARS) ~ test(lastChar != '.')
      }

      // PN_LOCAL ::= (PN_CHARS_U | ':' | [0-9] | PLX) ((PN_CHARS | '.' | ':' | PLX)* (PN_CHARS | ':' | PLX))?
      def PN_LOCAL: Rule1[String] = rule (
        clearSB() ~ (PN_CHARS_U | (CharPredicate(':') ++ CharPredicate.Digit) ~ appendSB() | PLX) ~ optional(PN_LOCAL2) ~ push(sb.toString())
      )

      // (PN_CHARS | '.' | ':' | PLX)* (PN_CHARS | ':' | PLX)
      // so basically (PN_CHARS | '.' | ':' | PLX)+ with no trailing '.'
      def PN_LOCAL2: Rule0 = rule (
        oneOrMore(PN_CHARS | anyOf(".:") ~ appendSB() | PLX) ~ test(lastChar != '.')
      )

      // PLX ::= PERCENT | PN_LOCAL_ESC
      def PLX: Rule0 = rule (
        PERCENT | PN_LOCAL_ESC
      )

      // PERCENT ::= '%' HEX HEX
      // HEX ::= [0-9] | [A-F] | [a-f]
      def PERCENT: Rule0 = rule (
        '%' ~ appendSB() ~ CharPredicate.HexDigit ~ appendSB() ~ CharPredicate.HexDigit ~ appendSB()
      )

      // PN_LOCAL_ESC ::= '\' ('_' | '~' | '.' | '-' | '!' | '$' | '&' | "'" | '(' | ')' | '*' | '+' | ',' | ';' | '=' | '/' | '?' | '#' | '@' | '%')
      def PN_LOCAL_ESC: Rule0 = rule (
        '\\' ~ appendSB() ~ anyOf("_~.-!$&'()*+,;=/?#@%") ~ appendSB()
      )
  
    }

  }

}
