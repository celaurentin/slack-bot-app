import sbt.Keys._
import sbt._

val scala3Version = "3.0.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "slack-bot-app",
    version := "0.1.0",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "com.slack.api" % "bolt-socket-mode" % "1.39.3",
      "org.glassfish.tyrus.bundles" % "tyrus-standalone-client" % "1.19",
      "org.slf4j" % "slf4j-simple" % "1.7.31"
    ),
    scalacOptions ++= Seq(
      "-language:postfixOps",
      "-Ykind-projector",
      "-source", "future",
      "-Xfatal-warnings"
    )
  )