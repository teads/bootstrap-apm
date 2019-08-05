package tv.teads.datadog

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, Uri }
import akka.http.scaladsl.server.Directives
import datadog.trace.api.DDTags
import io.opentracing.util.GlobalTracer

import scala.concurrent.{ ExecutionContext, Future }

object RouterBind extends Directives {

  def routes(implicit ec: ExecutionContext): HttpRequest => Future[HttpResponse] = {
    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>

      val tracer = GlobalTracer.get()
      val scope = tracer.buildSpan("TEST").startActive(true)
      scope.span().setTag(DDTags.SERVICE_NAME, "DATADOG-TESTING")

      Thread.sleep(1000)

      scope.close()

      Future.successful(HttpResponse(entity = "Service ready to run"))

    case HttpRequest(GET, Uri.Path("/test"), _, _, _) =>

      val f1 = Future {
        val tracer = GlobalTracer.get()
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

      f1.flatMap(f11 => f2.map(f22 => f11 + f22)).map { response =>
        HttpResponse(entity = response)
      }

    case HttpRequest(GET, Uri.Path("/test2"), _, _, _) =>

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

      f.map(response => HttpResponse(entity = response))
  }

}
