name := "namegen"
version := "0.1"
scalaVersion := "3.2.2"

val circeVersion = "0.14.5"
val http4sVersion = "1.0.0-M35"

libraryDependencies ++= Vector(
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-prometheus-metrics" % http4sVersion,

  "ch.qos.logback" % "logback-classic" % "1.2.11"
)

mainClass := Some("namegen.Main")

scalacOptions ++= Seq(
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked"                         // Enable additional warnings where generated code depends on assumptions.
)

scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings")
