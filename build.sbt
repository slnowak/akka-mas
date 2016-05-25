import sbt.Keys._

name := "akka-mas"
version := "1.0"
scalaVersion := "2.11.8"

val akkaVersion = "2.4.6"
val kamonVersion = "0.6.1"


val apacheCommonsMath = "org.apache.commons" % "commons-math3" % "3.6.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "io.kamon" %% "kamon-core" % kamonVersion,
  "io.kamon" %% "kamon-statsd" % kamonVersion,
  "io.kamon" %% "kamon-log-reporter" % kamonVersion,
  "io.kamon" %% "kamon-akka" % kamonVersion,
  "io.kamon" %% "kamon-akka-remote_akka-2.4" % kamonVersion,
  "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)
libraryDependencies += "org.apache.commons" % "commons-math3" % "3.6.1"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.7" % "runtime"


mainClass in(Compile, run) := Some("pl.edu.agh.akka.mas.App")

aspectjSettings

javaOptions in run <++= AspectjKeys.weaverOptions in Aspectj

fork in run := true
