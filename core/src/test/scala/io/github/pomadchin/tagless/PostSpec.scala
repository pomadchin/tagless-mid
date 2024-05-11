package io.github.pomadchin.tagless

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.semigroup.*
import cats.syntax.either.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach

class PostSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with Fixtures with BeforeAndAfterEach {
  "Post Spec" - {
    "should construct a service with attached logging and tracing" in {
      val service: Service[IO] = Service.instance[IO]
      val logging: Service[Post[IO, *]] = Service.loggingPostInstance[IO]

      // attach logging post to the service
      val serviceTracedAndLogged: Service[IO] = logging.attach(service)

      serviceTracedAndLogged.list.asserting(_ shouldBe serviceListValues.asRight) >>
        logMapActualValues[IO].asserting(_ shouldBe List("middle", "after"))
    }
  }

  override def afterEach(): Unit = clearLog
}
