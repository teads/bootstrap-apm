package tv.teads.newrelic

import akka.http.scaladsl.server.{ Directives, Route }
import com.newrelic.api.agent.Trace

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object Router extends Directives {

  @Trace(dispatcher = true)
  private def doStuff(v: Int): Int = {
    println("I'm doing stuff slowly")
    Thread.sleep(500)
    v + 1
  }

  @Trace(async = true)
  private def doAsyncStuff(v: Int)(implicit ec: ExecutionContext): Future[String] = Future {
    println("Async")
    Thread.sleep(500)
    s"Async number ${v + 2}"
  }

  def routes(implicit executionContext: ExecutionContext): Route =
    (pathEndOrSingleSlash | pathPrefix("ping")) {
      pathEndOrSingleSlash {
        Thread.sleep(1000)
        complete("Service ready to run")
      }
    } ~ pathPrefix("test") {
      pathEndOrSingleSlash {

        val v1 = doStuff(5)

        val f1 = Future {
          Thread.sleep(1000)
        }.flatMap { _ =>
          Future {
            Thread.sleep(1000)
            "Hello "
          }
        }

        val f2 = Future {

          Thread.sleep(500)

          doAsyncStuff(v1).foreach(println)

          val res = doStuff(v1)

          Thread.sleep(1000)

          s"world $res"
        }

        onComplete(f1.flatMap(f11 => f2.map(f22 => f11 + f22))) {
          case Failure(error)    => complete(error)
          case Success(response) => complete(response)
        }
      }
    }

}
