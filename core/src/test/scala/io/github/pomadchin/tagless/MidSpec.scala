package io.github.pomadchin.tagless

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.semigroup.*
import cats.syntax.either.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach

class MidSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with Fixtures with BeforeAndAfterEach {
  "Mid Spec" - {
    "should construct a service with attached logging and tracing" in {
      val service: Service[IO] = Service.instance[IO]
      val logging: Service[Mid[IO, *]] = Service.loggingMidInstance[IO]
      val tracing: Service[Mid[IO, *]] = Service.tracingMidInstance[IO]

      // attach tracing and logging mid to the service
      val serviceTracedAndLogged: Service[IO] = (tracing |+| logging).attach(service)

      serviceTracedAndLogged.list.asserting(_ shouldBe serviceListValues.asRight) >>
        logMapActualValues[IO].asserting(_ shouldBe List("before", "middle", "after"))
    }

    "should construct a service with attached logging and tracing, Mids derived via Pre and Post" in {
      val service: Service[IO] = Service.instance[IO]
      val logging: Service[Mid[IO, *]] = Service.loggingMidViaPostInstance[IO]
      val tracing: Service[Mid[IO, *]] = Service.tracingMidViaPreInstance[IO]

      // attach tracing and logging mid to the service
      val serviceTracedAndLogged: Service[IO] = (tracing |+| logging).attach(service)

      serviceTracedAndLogged.list.asserting(_ shouldBe serviceListValues.asRight) >>
        logMapActualValues[IO].asserting(_ shouldBe List("before", "middle", "after"))
    }
  }

  override def afterEach(): Unit = clearLog
}
