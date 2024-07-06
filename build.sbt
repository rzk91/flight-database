val scalaV = "2.13.13"

lazy val commonSettings = Seq(
  organization := "rzk.scala",
  version      := "1.0-SNAPSHOT",
  scalaVersion := scalaV,
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
  ),
  scalafixScalaBinaryVersion := scalaV.split('.').take(2).mkString(".")
)

val circeVersion = "0.14.3"
val doobieVersion = "1.0.0-RC5"
val http4sVersion = "0.23.26"
val pureconfigVersion = "0.17.7"
val flywayVersion = "10.14.0"
val scalaTestVersion = "3.2.19"
val testcontainersVersion = "0.41.3"

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

val enumerationDependencies = Seq(
  "com.beachape" %% "enumeratum" % "1.7.3"
)

val otherDependencies = Seq(
  "com.github.pureconfig"      %% "pureconfig"                % pureconfigVersion,
  "com.github.pureconfig"      %% "pureconfig-cats-effect"    % pureconfigVersion,
  "org.slf4j"                  % "slf4j-log4j12"              % "2.0.13",
  "com.typesafe.scala-logging" %% "scala-logging"             % "3.9.5",
  "commons-io"                 % "commons-io"                 % "2.16.1",
  "org.flywaydb"               % "flyway-core"                % flywayVersion,
  "org.flywaydb"               % "flyway-database-postgresql" % flywayVersion,
  "com.ibm.icu"                % "icu4j"                      % "75.1"
)

val allCoreDependencies =
  circeDependencies ++
    doobieDependencies ++
    http4sDependencies ++
    enumerationDependencies ++
    otherDependencies

val testingDependencies = Seq(
  "org.scalactic" %% "scalactic" % scalaTestVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "org.scalamock" %% "scalamock" % "6.0.0" % "test"
)

val itDependencies = Seq(
  "org.scalatest" %% "scalatest"                       % scalaTestVersion      % "it",
  "com.dimafeng"  %% "testcontainers-scala-scalatest"  % testcontainersVersion % "it",
  "com.dimafeng"  %% "testcontainers-scala-postgresql" % testcontainersVersion % "it"
)

lazy val core = project
  .in(file("modules/core"))
  .settings(
    name := "flight-database-core",
    commonSettings,
    libraryDependencies ++= circeDependencies ++ doobieDependencies ++ enumerationDependencies ++ testingDependencies
  )

lazy val utils = project
  .in(file("modules/utils"))
  .dependsOn(core)
  .settings(
    name := "flight-database-utils",
    commonSettings,
    libraryDependencies ++= allCoreDependencies ++ testingDependencies,
    addCompilerPlugin(("org.typelevel" % "kind-projector" % "0.13.3").cross(CrossVersion.full))
  )

lazy val backend = project
  .in(file("modules/backend"))
  .dependsOn(core, utils)
  .configs(IntegrationTest)
  .settings(
    name := "flight-database-backend",
    commonSettings,
    Defaults.itSettings,
    scalafixConfigSettings(IntegrationTest),
    libraryDependencies ++= allCoreDependencies ++ testingDependencies ++ itDependencies,
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    addCompilerPlugin(("org.typelevel" % "kind-projector" % "0.13.3").cross(CrossVersion.full))
  )

// TODO: Add frontend module

lazy val root = (project in file("."))
  .aggregate(core, utils, backend)
  .settings(
    name := "flight-database",
    commonSettings,
  )

// Run integration tests in a separate JVM from sbt
IntegrationTest / fork := true

// For scalafix
inThisBuild(
  List(
    scalaVersion      := scalaV,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)
