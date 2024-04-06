val scalaV = "2.13.13"

scalaVersion := scalaV
name         := "flight-database"
organization := "rzk.scala"
version      := "1.0"

val circeVersion = "0.14.3"
val doobieVersion = "1.0.0-RC5"
val http4sVersion = "0.23.26"
val pureconfigVersion = "0.17.6"

val circeDependencies = Seq(
  "io.circe" %% "circe-core"           % circeVersion,
  "io.circe" %% "circe-generic"        % circeVersion,
  "io.circe" %% "circe-parser"         % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion
)

val doobieDependencies = Seq(
  "org.tpolecat" %% "doobie-core"      % doobieVersion,
  "org.tpolecat" %% "doobie-hikari"    % doobieVersion,
  "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion
)

val http4sDependencies = Seq(
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl"          % http4sVersion,
  "org.http4s" %% "http4s-circe"        % http4sVersion
)

val otherDependencies = Seq(
  "com.github.pureconfig"      %% "pureconfig"             % pureconfigVersion,
  "com.github.pureconfig"      %% "pureconfig-cats-effect" % pureconfigVersion,
  "org.slf4j"                  % "slf4j-log4j12"           % "2.0.9",
  "com.typesafe.scala-logging" %% "scala-logging"          % "3.9.5",
  "commons-io"                 % "commons-io"              % "2.15.1",
  "org.flywaydb"               % "flyway-core"             % "9.22.3"
)

val testingDependencies = Seq(
  "org.scalactic" %% "scalactic" % "3.2.17",
  "org.scalatest" %% "scalatest" % "3.2.17" % "test"
)

libraryDependencies ++= circeDependencies ++ doobieDependencies ++ http4sDependencies ++ otherDependencies ++ testingDependencies

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

// For scalafix
inThisBuild(
  List(
    scalaVersion      := scalaV,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

scalafixScalaBinaryVersion := scalaV.split('.').take(2).mkString(".")

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
  "-language:postfixOps",
  "-Wunused:imports"
)
