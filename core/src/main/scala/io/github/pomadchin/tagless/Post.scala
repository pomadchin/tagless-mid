package io.github.pomadchin.tagless

import cats.data.Tuple2K
import cats.tagless.ApplyK
import cats.{~>, Applicative, Apply, FlatMap, Monoid, MonoidK, Semigroup, SemigroupK}
import cats.syntax.flatMap.*
import cats.syntax.apply.*
import cats.syntax.applicative.*

/**
 * A function `[F[_], A] =>> A => F[Unit]` An algebra `U[Post[F, _]]` is an algebra which translates all actions to `A \=> F[Unit]`. This is useful to
 * represent actions succeeding main logic.
 */
trait Post[F[_], A] {
  def apply(a: A): F[Unit]
}

object Post extends PostInstances {

  /**
   * when unification fails
   */
  def attach[U[f[_]]: ApplyK, F[_]: FlatMap](up: U[Post[F, _]])(alg: U[F]): U[F] = up.attach(alg)

  def asMid[F[_]: FlatMap]: Post[F, _] ~> Mid[F, _] = FunctionKLift[Post[F, _], Mid[F, _]](p => fa => fa.flatTap(p(_)))

  implicit final class TofuPostAlgebraSyntax[F[_], U[f[_]]](private val self: U[Post[F, _]]) extends AnyVal {
    def attach(alg: U[F])(implicit U: ApplyK[U], F: FlatMap[F]): U[F] =
      U.map2K(alg, self)(FunctionKLift[Tuple2K[F, Post[F, _], _], F](t2k => t2k.first.flatTap(a => t2k.second(a))))
  }
}

class PostInstances extends PostInstances1 {
  implicit def postMonoidK[F[_]: Applicative]: MonoidK[Post[F, _]] = new PostMonoidK[F]
}

class PostInstances1 {
  implicit def postSemigroupK[F[_]: Apply]: SemigroupK[Post[F, _]] = new PostSemigroupK[F]

  implicit def postAlgebraSemigroup[F[_]: Apply, U[f[_]]: ApplyK]: Semigroup[U[Post[F, _]]] =
    new PostAlgebraSemigroup[F, U]
}

private class PostAlgebraSemigroup[F[_], U[f[_]]](implicit F: Apply[F], U: ApplyK[U]) extends Semigroup[U[Post[F, _]]] {
  def combine(x: U[Post[F, _]], y: U[Post[F, _]]): U[Post[F, _]] =
    U.map2K(x, y)(FunctionKLift[Tuple2K[Post[F, _], Post[F, _], _], Post[F, _]](t2k => a => t2k.first(a) *> t2k.second(a)))
}

private class PostSemigroupK[F[_]: Apply] extends SemigroupK[Post[F, _]] {
  def combineK[A](x: Post[F, A], y: Post[F, A]): Post[F, A] = a => x(a) *> y(a)
}

private class PostMonoidK[F[_]](implicit F: Applicative[F]) extends PostSemigroupK[F] with MonoidK[Post[F, _]] {
  def empty[A]: Post[F, A] = _ => F.unit
}
