package tv.teads.datadog

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{ HttpApp, Route }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import io.opentracing.Tracer
import io.opentracing.contrib.concurrent.TracedExecutionContext
import io.opentracing.util.GlobalTracer

import scala.concurrent.{ ExecutionContext, Future }

object Starter extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("actor-datadog")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  private val tracer: Tracer = GlobalTracer.get()
  private val _ec: ExecutionContext =
    actorSystem.dispatchers.lookup("akka.actor.default-dispatcher")
  implicit val ec: TracedExecutionContext = new TracedExecutionContext(_ec, tracer)

  object WebServerHighLevel extends HttpApp {

    override def routes: Route = Router.routes

  }

  object WebServerLowLevel {

    lazy val source: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
      Http().bind("0.0.0.0", 8080)

    lazy val bindingFuture: Future[Http.ServerBinding] =
      WebServerLowLevel.source.to(Sink.foreach { connection =>
        println("Accepted new connection from " + connection.remoteAddress)

        connection handleWithAsyncHandler RouterBind.routes
        // this is equivalent to
        // connection handleWith { Flow[HttpRequest] map requestHandler }
      }).run()

  }

  WebServerHighLevel.startServer("0.0.0.0", 8080)

}
