package nl.flotsam.rate

import org.specs2.mutable.Specification
import scala.concurrent._
import scala.concurrent.duration._

class RateLimitationSpec extends Specification {

  override def intToRichLong(v: Int) = super.intToRichLong(v)


  "The rate limiter" should {

    "allow you to prevent something from getting executed too often" in {
      val limit = RateLimit(2 per (10 seconds))
      def times2(i: Int) = i * 2
      val times2Limited = limit(times2 _)
      val attempts = for (i <- List(1, 2, 3)) yield times2Limited(i)

      Thread.sleep(5000)

      // There should only be two completed requests right now
      attempts must contain((f: Future[Int]) => f.isCompleted).exactly(2.times)

      Thread.sleep(6000)

      // Since we're well beyond the limit set, I'm expecting all futures to be completed
      attempts must contain((f: Future[Int]) => f.isCompleted).exactly(3.times)
    }

  }

}
