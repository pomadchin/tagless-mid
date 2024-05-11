package io.github.pomadchin.tagless

import cats.{~>, FlatMap}
import cats.data.Tuple2K
import cats.effect.{Clock, Sync}
import cats.syntax.either.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.tagless.*
import cats.tagless.syntax.functorK.*

import scala.collection.concurrent.TrieMap

trait Fixtures {
  // Nanos -> Value
  val logMap = new TrieMap[Long, String]()
  def clearLog: Unit = logMap.clear()

  def logMap[F[_]: Sync]: F[TrieMap[Long, String]] = Sync[F].delay(logMap)
  def logMapActualValues[F[_]: Sync]: F[List[String]] =
    logMap[F].map(_.toList.sortBy(_._1).map(_._2.split(", ").last.trim))

  trait Logger[F[_]] {
    def info(msg: String): F[Unit]
  }

  object Logger {
    def apply[F[_]](implicit ev: Logger[F]): Logger[F] = ev

    implicit def instance[F[_]: Sync: Clock]: Logger[F] = { msg =>
      Clock[F].monotonic.map { duration =>
        println(msg)
        logMap.putIfAbsent(duration.toNanos, msg)
      }.void
    }
  }

  case class User(id: Long, name: String)
  case class CrudError(msg: String = "error")

  val serviceListValues: List[User] = List(User(1, "user1"), User(2, "user2"))

  trait Service[F[_]] {
    def list: Service.Result[F]
  }

  object Service {
    implicit val applyKForService: ApplyK[Service] = Derive.applyK

    type Result[F[_]] = F[Either[CrudError, List[User]]]
    def instance[F[_]: Sync: Clock]: Service[F] = new Service[F] {

      def list: F[Either[CrudError, List[User]]] = Clock[F].monotonic.flatMap { duration =>
        Sync[F].delay {
          println(s"service.list, middle: $serviceListValues")
          logMap.putIfAbsent(duration.toNanos, "service.list, middle")
          serviceListValues.asRight
        }
      }
    }

    def tracingMidInstance[F[_]: FlatMap: Logger]: Service[Mid[F, *]] =
      new Service[Mid[F, *]] {
        def list: Mid[F, Either[CrudError, List[User]]] = Logger[F].info("tracing, before") >> _
      }

    def loggingMidInstance[F[_]: FlatMap: Logger]: Service[Mid[F, *]] =
      new Service[Mid[F, *]] {
        def list: Mid[F, Either[CrudError, List[User]]] = _.flatTap(_ => Logger[F].info("logging, after"))
      }

    def tracingMidViaPreInstance[F[_]: FlatMap: Logger]: Service[Mid[F, *]] =
      tracingPreInstance[F].mapK(Pre.asMid[F])

    def loggingMidViaPostInstance[F[_]: FlatMap: Logger]: Service[Mid[F, *]] =
      loggingPostInstance[F].mapK(Post.asMid[F])

    def loggingPostInstance[F[_]: FlatMap: Logger]: Service[Post[F, *]] =
      new Service[Post[F, *]] {
        def list: Post[F, Either[CrudError, List[User]]] = _ => Logger[F].info("logging, after")
      }

    def tracingPreInstance[F[_]: FlatMap: Logger]: Service[Pre[F, *]] =
      new Service[Pre[F, *]] {
        def list: Pre[F, Either[CrudError, List[User]]] = Pre.apply(Logger[F].info("tracing, before"))
      }
  }
}
