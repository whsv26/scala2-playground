package org.whsv26.playground.macroses

import io.estatico.newtype.Coercible

import scala.reflect.macros.blackbox
import scala.language.experimental.macros

class IdExtractorDerivation(val c: blackbox.Context) {
  import c.universe._

  /**
   * Macros implementation
   */
  final def impl[T: c.WeakTypeTag]: c.Expr[IdExtractor[T]] = {
    if (!(weakTypeOf[T] <:< typeOf[HasId[_]])) {
      c.abort(c.enclosingPosition, "Entity must implement foldi.macros.HasId trait")
    }

    val instanceType = appliedType(
      typeOf[IdExtractor[_]].typeConstructor,
      List(weakTypeOf[T])
    )

    val tree = whenStringId(instanceType)
      .orElse(whenNewTypeId(instanceType))
      .getOrElse(c.abort(c.enclosingPosition, "Only String and String @newtype ids allowed"))

    c.Expr[IdExtractor[T]](tree)
  }

  private def whenStringId[T: c.WeakTypeTag](instanceType: Type): Option[Tree] =
    Option.when(weakTypeOf[T] <:< typeOf[HasId[String]]) {
      q"""
        new $instanceType {
          override def extract(t: ${weakTypeOf[T]}): String = t.id
        }
      """
    }

  private def whenNewTypeId[T: c.WeakTypeTag](instanceType: Type): Option[Tree] =
    Option.when(extractIdType[T].exists(isNewType)) {
      q"""
        new $instanceType {
          override def extract(t: ${weakTypeOf[T]}): String = t.id.asInstanceOf[String]
        }
      """
    }

  /**
   * Extract id field type
   */
  private def extractIdType[T: c.WeakTypeTag]: Option[c.universe.Type] =
    Some(weakTypeOf[T])
      .map(_.member(TermName("id")))
      .filter(_.isMethod)
      .map(_.asMethod.returnType)

  /**
   * Only newtypes have Coercible instances
   */
  private def isNewType(id: Type): Boolean =
    hasImplicit(
      typeOf[Coercible[_, _]].typeConstructor,
      List(id, typeOf[String])
    )

  /**
   * Check if implicit is found
   */
  private def hasImplicit(constructor: Type, args: List[Type]): Boolean =
    c.inferImplicitValue(appliedType(constructor, args), silent = true) match {
      case EmptyTree => false
      case _ => true
    }
}
