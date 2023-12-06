package io.github.pomadchin.tagless

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.semigroup.*
import cats.syntax.either.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

class MidSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  import io.github.pomadchin.tagless.Fixtures.*

  "Mid Spec" - {
    "should construct a service with attached logging and tracing" in {
      val service: Service[IO] = Service.instance[IO]
      val logging: Service[Mid[IO, *]] = Service.loggingInstance[IO]
      val tracing: Service[Mid[IO, *]] = Service.tracingInstance[IO]

      // attach tracing and logging mid to the service
      val serviceTracedAndLogged: Service[IO] = (tracing |+| logging).attach(service)

      serviceTracedAndLogged.list.asserting(_ shouldBe serviceListValues.asRight) >>
        logMapActualValues[IO].asserting(_ shouldBe List("before", "middle", "after"))
    }
  }
}
