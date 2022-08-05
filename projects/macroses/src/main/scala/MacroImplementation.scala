package org.whsv26.playground.macroses

import scala.reflect.macros.blackbox
import scala.language.experimental.macros

object MacroImplementation {
  trait HasId {
    def id: String
  }

  def macros[T]: T => String = macro impl[T]

  def impl[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[T => String] = {
    import c.universe._

    val functionType = appliedType(
      typeOf[(_) => _].typeConstructor,
      List(weakTypeOf[T], typeOf[String])
    )

    val extractor =
      if (weakTypeOf[T] <:< typeOf[HasId]) "v1.id"
      else "v1.id.value"

    val tree =
      q"""
        new $functionType {
          override def apply(v1: ${weakTypeOf[T]}): String = $extractor
        }
      """

    c.Expr[T => String](tree)
  }
}
