package org.whsv26.playground.macroses

import scala.reflect.macros.blackbox
import scala.language.experimental.macros

object MacroImplementation {
  trait HasId {
    def id: String
  }

  abstract class IdExtractor[T] {
    def extract(t: T): String
  }

  object IdExtractor {
    implicit def instance[T]: IdExtractor[T] = macro deriveInstance[T]
  }

  def deriveInstance[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[IdExtractor[T]] = {
    import c.universe._


    val extractorType = appliedType(typeOf[IdExtractor[_]].typeConstructor, List(weakTypeOf[T]))

    val tree = if (weakTypeOf[T] <:< typeOf[HasId]) {
      q"""
        new $extractorType {
          override def extract(t: ${weakTypeOf[T]}): String = t.id
        }
      """
    } else {
      q"""
        new $extractorType {
          override def extract(t: ${weakTypeOf[T]}): String = t.id.value
        }
      """
    }

    c.Expr[IdExtractor[T]](tree)
  }
}
