package io.github.pomadchin.tagless

import cats.data.{Chain, Tuple2K}
import cats.tagless.{ApplyK, InvariantK}
import cats.{~>, MonoidK, Semigroup}

trait Mid[F[_], A] {
  def apply(fa: F[A]): F[A]
  @inline def attach(fa: F[A]): F[A] = apply(fa)

  def compose(that: Mid[F, A]): Mid[F, A] = that.andThen(this)

  def andThen(that: Mid[F, A]): Mid[F, A] = Mid.MidCompose(Chain(this, that))
}

object Mid extends MidInstances {

  /**
   * when unification fails
   */
  def attach[U[f[_]]: ApplyK, F[_]](up: U[Mid[F, *]])(alg: U[F]): U[F] = up.attach(alg)

  implicit final class TofuMidAlgebraSyntax[F[_], U[f[_]]](private val self: U[Mid[F, *]]) extends AnyVal {
    def attach(alg: U[F])(implicit U: ApplyK[U]): U[F] =
      U.map2K(alg, self)(FunctionKLift[Tuple2K[F, Mid[F, *], *], F](fa => fa.second(fa.first)))
  }

  final private case class MidCompose[F[_], A](elems: Chain[Mid[F, A]]) extends Mid[F, A] {
    override def apply(fa: F[A]): F[A] = elems.foldLeft(fa)((x, m) => m(x))
    override def compose(that: Mid[F, A]): Mid[F, A] = that match {
      case MidCompose(es) => MidCompose(elems ++ es)
      case _              => MidCompose(elems :+ that)
    }
    override def andThen(that: Mid[F, A]): Mid[F, A] = that match {
      case MidCompose(es) => MidCompose(es ++ elems)
      case _              => MidCompose(that +: elems)
    }
  }
}

trait MidInstances extends MidInstances1 {
  implicit def midMonoidK[F[_]]: MonoidK[Mid[F, *]] = new MidMonoidK[F]
}

trait MidInstances1 {
  implicit def midAlgebraSemigroup[F[_], U[f[_]]: ApplyK]: Semigroup[U[Mid[F, *]]] = new MidAlgebraSemigroup[F, U]

  private val midInvariantInstance: InvariantK[({ type λ[F[_]] = Mid[F, Any] })#λ] = new MidInvariantK()
  implicit def midInvariantK[A]: InvariantK[({ type λ[F[_]] = Mid[F, A] })#λ] =
    midInvariantInstance.asInstanceOf[InvariantK[({ type λ[F[_]] = Mid[F, A] })#λ]]
}

class MidMonoidK[F[_]] extends MonoidK[Mid[F, *]] {
  def empty[A]: Mid[F, A] = fa => fa
  def combineK[A](x: Mid[F, A], y: Mid[F, A]): Mid[F, A] = fa => x(y(fa))
}

class MidInvariantK extends InvariantK[({ type λ[F[_]] = Mid[F, Any] })#λ] {
  def imapK[F[_], G[_]](af: Mid[F, Any])(fk: F ~> G)(gk: G ~> F): Mid[G, Any] = { ga =>
    fk(af(gk(ga)))
  }
}

class MidAlgebraSemigroup[F[_], U[f[_]]](implicit U: ApplyK[U]) extends Semigroup[U[Mid[F, *]]] {
  def combine(x: U[Mid[F, *]], y: U[Mid[F, *]]): U[Mid[F, *]] =
    U.map2K(x, y)(FunctionKLift[Tuple2K[Mid[F, *], Mid[F, *], *], Mid[F, *]](t2 => fa => t2.first(t2.second(fa))))
}
