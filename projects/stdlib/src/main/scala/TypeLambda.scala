package org.whsv26.playground.stdlib

object TypeLambda {
  def test[F[_], T](fa: F[T]): F[T] = identity(fa)

  val res: Either[String, Int] =
    test[({ type λ[α] = Either[String, α] })#λ, Int](Right(1))
}
