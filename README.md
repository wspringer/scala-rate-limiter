# Read me

Rate limiting should not be something you have to implement yourself. Ideally, you should be able to get it by just wrapping your function into something that magically transforms it into something that is making sure you're not invoking it too often (postponing it if you do invoke it more than you should.) That's what this prototype is doing.

It's currently limited to limiting `A => B` type of functions, that is functions with one argument only. But with the right implicits you should be able to get it working for functions up to 22 arguments. 

```scala
import nl.flotsam.rate._
import scala.concurrent._
import scala.concurrent.duration._

// For sake of example, we're using the global context
import ExecutionContext.global

def times2(i: Int) = i * 2

val limit = RateLimit(2 per (5 seconds))

val times2Limited = limit(times2 _)

times2Limited(2) // Future(4)
times2Limited(3) // Future(6)
times2Limited(4) // Will not start executing for a while
```

### Limitations

This particular implementation is blocking a thread for everything in a wait state. It does return a future, so your code will not be blocking on it, but there will be threads in a blocked state. Which is not exactly ideal, but was the quickest solution, using Google's Guava under the hood.