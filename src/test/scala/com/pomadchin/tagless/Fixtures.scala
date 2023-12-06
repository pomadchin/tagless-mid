package com.pomadchin.tagless

import cats.{~>, FlatMap}
import cats.data.Tuple2K
import cats.effect.{Clock, Sync}
import cats.syntax.either.*
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import cats.tagless.ApplyK

import scala.collection.concurrent.TrieMap

object Fixtures {
  // Nanos -> Value
  val logMap = new TrieMap[Long, String]()
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
    // can be derived via @autoApplyK in Scala 2
    implicit val applyKForService: ApplyK[Service] = new ApplyK[Service] {
      def mapK[F[_], G[_]](af: Service[F])(fk: F ~> G): Service[G] =
        new Service[G] {
          def list: Result[G] = fk(af.list)
        }

      def productK[F[_], G[_]](af: Service[F], ag: Service[G]): Service[({ type λ[a] = Tuple2K[F, G, a] })#λ] =
        new Service[({ type λ[a] = Tuple2K[F, G, a] })#λ] {
          def list: Result[Tuple2K[F, G, *]] = cats.tagless.catsTaglessApplyKForIdK.productK[F, G](af.list, ag.list)
        }
    }

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

    def tracingInstance[F[_]: FlatMap: Logger]: Service[Mid[F, *]] =
      new Service[Mid[F, *]] {
        def list: Mid[F, Either[CrudError, List[User]]] = Logger[F].info("tracing, before") >> _
      }

    def loggingInstance[F[_]: FlatMap: Logger]: Service[Mid[F, *]] =
      new Service[Mid[F, *]] {
        def list: Mid[F, Either[CrudError, List[User]]] = _.flatMap {
          case r @ Right(_)    => Logger[F].info("logging, after").as(r)
          case l @ Left(error) => Logger[F].info(error.toString).as(l)
        }
      }
  }
}
