package io.github.pomadchin.tagless

import cats.arrow.FunctionK

// https://github.com/typelevel/cats/issues/2553#issuecomment-493712879
// https://github.com/typelevel/cats/blob/v2.9.0/core/src/main/scala-2/src/main/scala/cats/arrow/FunctionKMacros.scala
private[tagless] object FunctionKLift {

  /**
   * Used in the signature of `lift` to emulate a polymorphic function type
   */
  protected type τ[F[_], G[_]]

  def apply[F[_], G[_]](f: F[τ[F, G]] => G[τ[F, G]]): FunctionK[F, G] =
    new FunctionK[F, G] {
      def apply[A](fa: F[A]): G[A] = f.asInstanceOf[F[A] => G[A]](fa)
    }
}
