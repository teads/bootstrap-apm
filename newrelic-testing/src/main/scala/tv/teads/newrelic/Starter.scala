package tv.teads.newrelic

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{ HttpApp, Route }
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

object Starter extends App {

  object WebServer extends HttpApp {

    implicit val actorSystem: ActorSystem = ActorSystem("actor-newrelic")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext =
      actorSystem.dispatchers.lookup("akka.actor.default-dispatcher")

    override def routes: Route = Router.routes

  }

  WebServer.startServer("0.0.0.0", 8080)

}
