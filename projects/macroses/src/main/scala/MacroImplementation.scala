package org.whsv26.playground.macroses

import io.estatico.newtype.Coercible

import scala.reflect.macros.blackbox
import scala.language.experimental.macros

object MacroImplementation {
  trait HasId {
    def id: String
  }

  trait Entity[T] {
    def id(t: T): String
  }

  object Entity {
    def apply[T: Entity]: Entity[T] = implicitly[Entity[T]]

    implicit def instance[T]: Entity[T] = macro deriveInstance[T]
  }

  def deriveInstance[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[Entity[T]] = {
    import c.universe._

    val classSymbol = weakTypeOf[T].typeSymbol.asClass

    val idFieldType = classSymbol
      .primaryConstructor
      .asMethod
      .paramLists
      .head
      .find(_.name.encodedName.toString == "id")
      .map(_.asTerm.typeSignature)

    val isHasId = weakTypeOf[T] <:< typeOf[HasId]

    val isNewType = idFieldType
      .map(tpe => c.inferImplicitValue(
        appliedType(typeOf[Coercible[_, _]].typeConstructor, List(tpe, typeOf[String])),
        silent = true
      ))
      .collect {
        case EmptyTree => false
        case _ => true
      }
      .getOrElse(false)

    val extractorType = appliedType(typeOf[Entity[_]].typeConstructor, List(weakTypeOf[T]))

    val tree = if (isHasId) {
      q"""
        new $extractorType {
          override def id(t: ${weakTypeOf[T]}): String = t.id
        }
      """
    } else if (isNewType) {
      q"""
        new $extractorType {
          override def id(t: ${weakTypeOf[T]}): String =
            Coercible[${idFieldType.get}, String].apply(t.id)
        }
      """
    } else {
      c.abort(c.enclosingPosition, "Only HasId and Newtype allowed")
    }

    c.Expr[Entity[T]](tree)
  }
}
