val root = (project in file(".")).aggregate(datadogTesting)
  .settings(name := "apm")

val javaFlags = Seq(
  "-Xmx2048m",
  "-XX:+UnlockExperimentalVMOptions",
  "-XX:+EnableJVMCI",
  "-XX:+UseJVMCICompiler",
  "-Djvmci.Compiler=graal",
  "-Dgraal.GraalArithmeticStubs=false"
)

val scalacFlags = Seq(
  "-Xfatal-warnings",
  "-language:experimental.macros",
  "-language:reflectiveCalls",
  "-language:higherKinds",
  "-language:existentials",
  "-deprecation",
  "-explaintypes",
  "-unchecked",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-unused:-implicits,-explicits,-privates,-locals,_",
  "-opt:-l:project,-l:classpath,_"
)

def dockerSettings(ports: Seq[Int]) = Seq(
  dockerBaseImage := "openjdk:11-jre-slim",
  maintainer := "SSP <innov-ssp@teads.tv>",
  dockerExposedPorts := ports
)

def module(_name: String, ports: Int*) =
  Project(_name, file(_name))
    .settings(
      name := _name,
      scalaVersion := "2.12.8",
      javaOptions ++= javaFlags,
      scalacOptions ++= scalacFlags,
      scalacOptions --= Seq("-target:jvm-1.7"),
      scalacOptions in (Compile, console) ~=
        (_.filterNot(flag => flag.startsWith("-Y") || flag.startsWith("-X"))),
      scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,
      cancelable in Global := true,
      checksums in update := Nil
    )
    .settings(dockerSettings(ports): _*)
    .settings(libraryDependencies ++= List(
      "com.typesafe.akka" %% "akka-http" % Versions.akkaHttp,
      "com.typesafe.akka" %% "akka-stream" % Versions.akka,
      "com.typesafe.akka" %% "akka-actor"  % Versions.akka
    ))
    .enablePlugins(JavaServerAppPackaging, JavaAgent, AshScriptPlugin)

lazy val datadogTesting = module("datadog-testing", 8080)
  .settings(javaAgents += "com.datadoghq" % "dd-java-agent" % Versions.datadog)
  .settings(libraryDependencies ++= List(
    "com.datadoghq" % "dd-trace-api" % Versions.datadog,
    "io.opentracing" % "opentracing-api" % Versions.openTracing,
    "io.opentracing" % "opentracing-util" % Versions.openTracing,
    "io.opentracing.contrib" %% "opentracing-scala-concurrent" % Versions.openTracingConcurrent
  ))

lazy val newRelicTesting = module("newrelic-testing", 8080)
  .settings(libraryDependencies ++= List(
    "com.newrelic.agent.java" % "newrelic-api" % Versions.newRelic
  ))
  .enablePlugins(NewRelic)
  .settings(
    newrelicAppName := "newrelic-testing",
    newrelicVersion := "5.2.0",
    newrelicLicenseKey := sys.env.get("NEWRELIC_LICENSE"),
    newrelicCustomTracing := true,
    newrelicFuturesAsSegments := true
  )
