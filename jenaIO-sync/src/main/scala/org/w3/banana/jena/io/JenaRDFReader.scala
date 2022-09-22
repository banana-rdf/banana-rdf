package org.w3.banana.jena.io

import io.lemonlabs.uri.AbsoluteUrl
import org.apache.commons.io.input.ReaderInputStream
import org.apache.jena.graph.{Factory, NodeFactory, Node as JenaNode, Triple as JenaTriple}
import org.apache.jena.riot.*
import org.apache.jena.riot.system.*
import org.w3.banana.RDF.{Graph, rGraph}
import org.w3.banana.io.*
import org.w3.banana.jena.JenaRdf

import java.io.*
import java.nio.charset.Charset
import scala.util.*


object JenaRDFReader:
   type RDFReaderT[S] = RDFReader[JenaRdf.type, Try, S]
   type RDFrelReaderT[S] = RelRDFReader[JenaRdf.type, Try, S]

   def makeRDFReader[S](lang: Lang): RDFReaderT[S] =
     new RDFReader[JenaRdf.type, Try, S]:

        import JenaRdf.ops
        
        def read(is: InputStream, base: AbsoluteUrl): Try[Graph[JenaRdf.type]] = Try {
          val graph: org.apache.jena.graph.Graph = Factory.createDefaultGraph.nn
          val builder: RDFParserBuilder = RDFParser.create().nn
          { import scala.language.unsafeNulls
            builder.source(is)
              .lang(lang)
              .base(base.toString)
              .parse(graph)
          }
          graph.asInstanceOf[Graph[JenaRdf.type]]
        }

        def read(reader: Reader, base: AbsoluteUrl): Try[Graph[JenaRdf.type]] = Try {
          val graph: org.apache.jena.graph.Graph = Factory.createDefaultGraph.nn
          val builder: RDFParserBuilder = RDFParser.create().nn
          // why is Jena deprecating Readers, which should be the correct level to parse character based documents?
          { import scala.language.unsafeNulls
            builder.source(new ReaderInputStream(reader, Charset.forName("utf-8")))
              .lang(lang)
              .base(base.toString)
              .parse(graph)
          }
          graph.asInstanceOf[Graph[JenaRdf.type]]
        }
   end makeRDFReader
   
   def makeRDFRelReader[S](lang: Lang): RDFrelReaderT[S] =
      new RelRDFReader[JenaRdf.type ,Try, S]:
        def read(is: InputStream): Try[rGraph[JenaRdf.type]] = Try {
          val graph: org.apache.jena.graph.Graph = Factory.createDefaultGraph.nn
          val builder: RDFParserBuilder = RDFParser.create().nn
          { import scala.language.unsafeNulls
            builder.source(is)
              .lang(lang)
              .parse(graph)
          }
          graph.asInstanceOf[Graph[JenaRdf.type]]
        }

        def read(reader: Reader): Try[rGraph[JenaRdf.type]] = Try {
          val graph: org.apache.jena.graph.Graph = Factory.createDefaultGraph.nn
          val builder: RDFParserBuilder = RDFParser.create().nn
          { import scala.language.unsafeNulls
            builder.source(new ReaderInputStream(reader, Charset.forName("utf-8")))
              .lang(lang)
              .parse(graph)
          }
          graph.asInstanceOf[Graph[JenaRdf.type]]
        }

   given rdfxmlReader: RDFReaderT[RDFXML] = makeRDFReader[RDFXML](Lang.RDFXML.nn)
   given rdfxmlRelReader: RDFrelReaderT[RDFXML] = makeRDFRelReader[RDFXML](Lang.RDFXML.nn)

   given turtleReader: RDFReaderT[Turtle] = makeRDFReader[Turtle](Lang.TURTLE.nn)
   given turtleRelReader: RDFrelReaderT[Turtle] = makeRDFRelReader[Turtle](Lang.TURTLE.nn)
   

  // given n3Reader: RDFReaders[N3] = makeRDFReader[N3](Lang.N3.nn)

   given jsonLdReader: RDFReaderT[JsonLdCompacted] = makeRDFReader[JsonLdCompacted](Lang.JSONLD11.nn)
   given jsonLdRelReader: RDFrelReaderT[JsonLdCompacted] = makeRDFRelReader[JsonLdCompacted](Lang.JSONLD11.nn)

end JenaRDFReader
