name := "akka-mas"
version := "1.0"
scalaVersion := "2.11.8"

val akkaVersion = "2.4.3"

val apacheCommonsMath = "org.apache.commons" % "commons-math3" % "3.6.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

libraryDependencies += apacheCommonsMath

mainClass in (Compile, run) := Some("pl.edu.agh.akka.mas.App")
