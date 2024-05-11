package io.github.pomadchin.tagless

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.semigroup.*
import cats.syntax.either.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach

class PreSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with Fixtures with BeforeAndAfterEach {
  "Pre Spec" - {
    "should construct a service with attached logging and tracing" in {
      val service: Service[IO] = Service.instance[IO]
      val tracing: Service[Pre[IO, *]] = Service.tracingPreInstance[IO]

      // attach tracing pre to the service
      val serviceTracedAndLogged: Service[IO] = tracing.attach(service)

      serviceTracedAndLogged.list.asserting(_ shouldBe serviceListValues.asRight) >>
        logMapActualValues[IO].asserting(_ shouldBe List("before", "middle"))
    }
  }

  override def afterEach(): Unit = clearLog
}
