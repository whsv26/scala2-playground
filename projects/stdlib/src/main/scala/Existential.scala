package org.whsv26.playground.stdlib

import scala.language.implicitConversions

object Existential {

  /**
   * A simple typeclass that converts types A to some specific Java type
   */
  trait AllowedType[A] {

    type JavaType >: Null <: AnyRef

    def toJavaType(a: A): JavaType

    def toObject(a: A): Object = toJavaType(a)
  }

  object AllowedType {
    def apply[A](implicit ev: AllowedType[A]): AllowedType[A] = ev

    /**
     * Helper instance creator
     */
    def mkInstance[A, J >: Null <: AnyRef](f: A => J): AllowedType[A] =
      new AllowedType[A] {
        type JavaType = J
        def toJavaType(a: A): J = f(a)
      }

    implicit val intInstance: AllowedType[Int] =
      mkInstance(Int.box)

    implicit val strInstance: AllowedType[String] =
      mkInstance(identity)

    implicit val boolInstance: AllowedType[Boolean] =
      mkInstance(Boolean.box)

    // We can create an instance for TCBox itself! It is simply the inner
    // type's instance.
    implicit def instanceForAny: AllowedType[TCBox[AllowedType]] =
      mkInstance(ev => ev.evidence.toJavaType(ev.value))
  }

  // data TCBox tc = forall a. tc a => TCBox a
  sealed trait TCBox[TC[_]] {
    type T
    val value: T
    val evidence: TC[T]
  }

  private case class MkTCBox[A, TC[_]](value: A)(implicit
    val evidence: TC[A]
  ) extends TCBox[TC] {
    type T = A
  }

  object TCBox {

    def apply[T, TC[_]](value: T)(implicit ev: TC[T]): TCBox[TC] =
      MkTCBox(value)

    /**
     * Allows for case matching with evidence
     */
    def unapply[TC[_]](t: TCBox[TC]): Option[(t.T, TC[t.T])] =
      Some(t.value -> t.evidence)

    // If `A` implements the type class `TC`, then `A` can be wrapped by
    // `TCBox[TC]` if it is expected. This allows for automatic lifting of
    // types into TCBox.
    implicit def anyToBox[TC[_], A: TC](v: A): TCBox[TC] = TCBox(v)
  }

  object Example {
    def bind(objs: Object*): Unit = ()

    type AnyAllowedType = TCBox[AllowedType]
    def toObj(t: AnyAllowedType) = AllowedType[AnyAllowedType].toObject(t)

    def bindT(objs: AnyAllowedType*): Unit =
      bind(objs.map(toObj):_*)

    def f(): Unit = bindT(123, "Hello", true)
  }
}
