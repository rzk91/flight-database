scalaVersion := "2.13.8"
name         := "flight-database"
organization := "rzk.scala"
version      := "1.0"

libraryDependencies ++= circeDependencies ++ doobieDependencies ++ http4sDependencies ++ otherDependencies

val circeVersion = "0.14.1"
val doobieVersion = "1.0.0-RC1"
val http4sVersion = "0.23.18"

val circeDependencies = Seq(
  "io.circe" %% "circe-core"           % circeVersion,
  "io.circe" %% "circe-generic"        % circeVersion,
  "io.circe" %% "circe-parser"         % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion
)

val doobieDependencies = Seq(
  "org.tpolecat" %% "doobie-core"     % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari"   % doobieVersion
)

val http4sDependencies = Seq(
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl"          % http4sVersion,
  "org.http4s" %% "http4s-circe"        % http4sVersion
)

val otherDependencies = Seq(
  "com.github.pureconfig"      %% "pureconfig"    % "0.17.2",
  "org.slf4j"                  % "slf4j-log4j12"  % "1.7.36",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
  "commons-io"                 % "commons-io"     % "2.11.0",
  "org.flywaydb"               % "flyway-core"    % "9.14.1"
)

scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  "-Xfatal-warnings",
  "-deprecation",
  "-unchecked",
  "-explaintypes",
  "-Ymacro-annotations",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)
