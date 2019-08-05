package tv.teads.datadog

import akka.http.scaladsl.server.{ Directives, Route }
import datadog.trace.api.DDTags
import io.opentracing.util.GlobalTracer

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object Router extends Directives {

  def routes(implicit executionContext: ExecutionContext): Route =
    (pathEndOrSingleSlash | pathPrefix("ping")) {
      pathEndOrSingleSlash {
        val tracer = GlobalTracer.get()
        val scope = tracer.buildSpan("TEST1").startActive(true)
        scope.span().setTag(DDTags.SERVICE_NAME, "DATADOG-TESTING")

        Thread.sleep(200)

        val scope2 = tracer.buildSpan("TEST2").startActive(true)
        scope2.span().setTag(DDTags.SERVICE_NAME, "DATADOG-TESTING")

        Thread.sleep(300)

        scope2.close()

        Thread.sleep(500)

        scope.close()

        complete("Service ready to run")
      }
    } ~ pathPrefix("ping2") {
      pathEndOrSingleSlash {
        val tracer = GlobalTracer.get()
        val scope = tracer.buildSpan("TEST11").startActive(true)
        scope.span().setTag(DDTags.SERVICE_NAME, "DATADOG-TESTING")

        Thread.sleep(200)

        Future {
          val scope2 = tracer.buildSpan("TEST22").startActive(true)
          scope2.span().setTag(DDTags.SERVICE_NAME, "DATADOG-TESTING")

          Thread.sleep(300)

          scope2.close()
        }

        Thread.sleep(500)

        scope.close()

        complete("Service ready to run")
      }
    } ~ pathPrefix("test") {
      pathEndOrSingleSlash {

        val tracer = GlobalTracer.get()
        val glob = tracer.buildSpan("FUT").startActive(true)
        glob.span().setTag(DDTags.SERVICE_NAME, "DATADOG-TESTING")

        val f1 = Future {

          val scope = tracer.buildSpan("FUT1").startActive(true)
          scope.span().setTag(DDTags.SERVICE_NAME, "DATADOG-TESTING")

          Thread.sleep(1000)

          scope.close()
        }.flatMap { _ =>
          Future {
            val tracer = GlobalTracer.get()
            val scope = tracer.buildSpan("FUT11").startActive(true)
            scope.span().setTag(DDTags.SERVICE_NAME, "DATADOG-TESTING")

            Thread.sleep(1000)

            scope.close()

            "Hello "
          }
        }

        val f2 = Future {
          val tracer = GlobalTracer.get()
          val scope = tracer.buildSpan("FUT2").startActive(true)
          scope.span().setTag(DDTags.SERVICE_NAME, "DATADOG-TESTING")

          Thread.sleep(500)

          Future {
            val scope2 = tracer.buildSpan("FUT22").startActive(true)
            scope2.span().setTag(DDTags.SERVICE_NAME, "DATADOG-TESTING")

            Thread.sleep(200)

            scope2.close()
          }

          Thread.sleep(1000)

          scope.close()

          "world"

        }

        onComplete(f1.flatMap(f11 => f2.map(f22 => f11 + f22))) {
          case Failure(error)    =>
            glob.close()
            complete(error)
          case Success(response) =>
            glob.close()
            complete(response)
        }
      }
    } ~ pathPrefix("test2") {
      pathEndOrSingleSlash {

        val tracer = GlobalTracer.get()
        val s1 = tracer.buildSpan("S11").start()
        val s11 = tracer.buildSpan("S111").startActive(true)
        val s2 = tracer.buildSpan("S22").asChildOf(s1)
        val s3 = tracer.buildSpan("S33").asChildOf(s1)
        val s33 = tracer.buildSpan("S333").asChildOf(s11.span())

        val f = Future {
          s1.setTag(DDTags.SERVICE_NAME, "DATADOG-TESTING")

          Thread.sleep(500)

          Future {
            val scope2 = s2.start()
            scope2.setTag(DDTags.SERVICE_NAME, "DATADOG-TESTING")
            val scope3 = s3.startActive(true)
            scope3.span().setTag(DDTags.SERVICE_NAME, "DATADOG-TESTING")
            val scope33 = s33.startActive(true)
            scope33.span().setTag(DDTags.SERVICE_NAME, "DATADOG-TESTING")
            Thread.sleep(200)
            scope2.finish()
            scope3.close()
            scope33.close()
          }

          Thread.sleep(1000)
          s1.finish()

          "Great"
        }

        onComplete(f) {
          case Failure(error)    => complete(error)
          case Success(response) => complete(response)
        }
      }
    }

}
