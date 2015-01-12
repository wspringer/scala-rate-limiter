package nl.flotsam

import com.google.common.util.concurrent.RateLimiter

import scala.concurrent._
import scala.concurrent.duration._

package object rate {

  case class Rate(number: Int, duration: Duration) {
    override def toString: String = s"$number per $duration"
  }

  case class RateLimit(rate: Rate) {

    private val limiter = RateLimiter.create(rate.number / (rate.duration / (1 second)))

    def apply[A,B](fn: A)(implicit policy: RateLimitingPolicy[A,B]): B = {
      policy.convert(fn, limiter)
    }

  }

  implicit class RateBuilder(val number: Int) extends AnyVal {

    def per(duration: Duration) = Rate(number, duration)

    def / (duration: Duration) = Rate(number, duration)

  }

  trait RateLimitingPolicy[A,B] {

    def convert(fn: A, rateLimiter: RateLimiter): B

  }

  implicit def fn1RateLimitable[A,B](implicit executionContext: ExecutionContext): RateLimitingPolicy[A => B, A => Future[B]] =
    new RateLimitingPolicy[A => B, A => Future[B]] {
      override def convert(fn: (A) => B, rateLimiter: RateLimiter): (A) => Future[B] =
        (a: A) =>
          future {
            rateLimiter.acquire()
            fn(a)
          }
    }
  
}
