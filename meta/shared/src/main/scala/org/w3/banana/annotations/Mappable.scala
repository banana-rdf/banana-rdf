package org.w3.banana.annotations

import scala.collection.immutable.Map
import scala.language.experimental.macros
import scala.reflect.macros._
import org.w3.banana.{RDF,RDFOps}

/**
 * base prefix
 * @param base default prefix
 */
class vocab(base:String) extends scala.annotation.ClassfileAnnotation

/**
 * URI for each property
 * @param name if no name is provided, takes term name
 */
class prop(name:String ="") extends scala.annotation.ClassfileAnnotation

trait Mappable[T] {
  def toMap[Rdf<:RDF](t: T)(implicit ops:RDFOps[Rdf]): Map[Rdf#URI, Any]
}

/**
 * This object contains macros that extracts properties from annotations and makes maps
 */
object Mappable {

  implicit def materializeMappable[T]: Mappable[T] = macro materializeMappableImpl[T]


  /**
   * Creates a map of rdf properties
   * @param c macro context
   * @tparam T type
   * @return Map[Rdf#URI,Any]
   */
  def materializeMappableImpl[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[Mappable[T]] = {
    import c.universe._
    /**
     * Extracting value of first param of annotation
     * @param anno
     * @return
     */
    def extract(anno:Annotation):String =  {
      val ch = anno.tree.children
      if(ch.size>1){
        val args = ch.tail.head.children
        c.eval(c.Expr[String](args.tail.head))
      }  else ""
    }

    def join(voc:String,url:String) = voc.last match {
      case '#' | '/'=> voc + url
      case other => voc+'/'+url
    }

    val tpe =c.weakTypeOf[T]
    val pte= c.weakTypeOf[prop]
    val vte = c.weakTypeOf[vocab]
    val vocs = for{
      v <- tpe.typeSymbol.asClass.annotations
      if v.tree.tpe =:= vte
    } yield extract(v)


    val toMapParams = for{
      m<-tpe.members
      if m.isTerm
      term = m.asTerm
      if term.isVal || term.isVar || term.isAccessor || term.isCaseAccessor || term.isGetter
      anno <- term.annotations
      if anno.tree.tpe =:= pte
      param = if(term.isVal || term.isVar) term.getter.asTerm else term.accessed.asTerm
      value = param.name
      url = extract(anno).replace(" ","_")
      if url.contains(":") || vocs.size>0
      name =  if(url.contains(":")) url else if(url.size>0) join(vocs.head,url) else join(vocs.head,term.name.decodedName.toString.trim)
    } yield {
      q"ops.makeUri($name) -> t.$value"
    }

    c.Expr[Mappable[T]] { q"""
      new Mappable[$tpe] {
        def toMap[Rdf<:org.w3.banana.RDF](t: $tpe)(implicit ops:org.w3.banana.RDFOps[Rdf]): Map[Rdf#URI, Any] = Map(..$toMapParams)
      }
    """ }
  }
}