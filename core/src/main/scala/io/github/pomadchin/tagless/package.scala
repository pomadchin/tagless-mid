package io.github.pomadchin

package object tagless {
  type Pre[F[_], A] = Pre.T[F, A]
}
