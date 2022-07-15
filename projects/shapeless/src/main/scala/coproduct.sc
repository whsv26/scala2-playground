import cats.data.EitherT
import cats.syntax.applicative._
import cats.{Applicative, Functor, Monad}
import shapeless._
import shapeless.ops.coproduct.Inject

implicit class CoproductEitherT[F[_]: Functor, A, B](underlying: EitherT[F, A, B]) {
  def widenLeft[A1 <: Coproduct](implicit
    inj: Inject[A1, A]
  ): EitherT[F, A1, B] = underlying.leftMap(Coproduct[A1](_))
}

class FooService[F[_]: Applicative] {
  def doSomething(): EitherT[F, FooService.Error, Int] =
    EitherT.right(3.pure[F])
}

object FooService {
  type Error = Error1.type :+: Error2 :+: CNil
  case object Error1
  case class Error2(kek: String)
}

class BarService[F[_]: Applicative] {
  def doSomething(): EitherT[F, BarService.Error, Int] =
    EitherT.right(2.pure[F])
}

object BarService {
  type Error = Error3 :+: CNil
  case class Error3(lol: String)
}

class BazService[F[_]: Monad](
  fooService: FooService[F],
  barService: BarService[F],
) {
  def doSomething(): EitherT[F, BazService.Error, Int] =
    for {
      a <- fooService.doSomething().widenLeft
      b <- barService.doSomething().widenLeft[BazService.Error]
    } yield a + b

  def doAndHandle(): EitherT[F, String, Int] = {
    object errorHandler extends Poly1 {
      implicit val caseErr1 = at[FooService.Error1.type](_ => "FooService error1")
      implicit val caseErr2 = at[FooService.Error2](_.kek)
      implicit val caseErr3 = at[BarService.Error3](_.lol)
    }

    doSomething().leftMap(_.adjoined.fold(errorHandler))
  }
}

object BazService {
  type Error = FooService.Error :+: BarService.Error :+: CNil
}