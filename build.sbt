import sbt.Keys._
import sbt._

val scala3Version = "3.3.3"

//lazy val ollama4z = RootProject(file("../ollama4z"))

lazy val root = project
  .in(file("."))
  .settings(
    name         := "slack-bot-app",
    version      := "0.1.0",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "com.lihaoyi"                   %% "upickle"                 % "3.3.0",
      "com.github.blemale"            %% "scaffeine"               % "5.2.1",
      "com.slack.api"                  % "bolt-socket-mode"        % "1.39.3",
      "com.softwaremill.sttp.client4" %% "core"                    % "4.0.0-M6",
      "org.glassfish.tyrus.bundles"    % "tyrus-standalone-client" % "1.19",
      "org.slf4j"                      % "slf4j-simple"            % "1.7.31",
      "dev.zio"                       %% "zio"                     % "2.1.1",
      "dev.zio"                       %% "zio-test"                % "2.1.1" % Test,
      "dev.zio"                       %% "zio-json"                % "0.7.0",
      "dev.zio"                       %% "zio-test-sbt"            % "2.1.1" % Test,
      "dev.zio"                       %% "zio-http"                % "3.0.0-RC8",
      "dev.zio"                       %% "zio-schema-json"         % "1.2.1",
      "com.github.devcdcc"            %% "ollama-zio"              % "0.1.0"
    ),
    scalacOptions ++= Seq(
      "-language:postfixOps",
      "-Ykind-projector",
      "-source",
      "future",
      "-Xfatal-warnings"
    )
  )
//  .dependsOn(ollama4z)
