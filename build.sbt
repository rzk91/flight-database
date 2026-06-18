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
  semanticdbEnabled          := true,
  semanticdbVersion          := scalafixSemanticdb.revision,
  scalafixScalaBinaryVersion := scalaV.split('.').take(2).mkString("."),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  addCompilerPlugin(("org.typelevel" % "kind-projector" % "0.13.3").cross(CrossVersion.full))
)

val circeVersion = "0.14.4"
val doobieVersion = "1.0.0-RC8"
val http4sVersion = "0.23.30"
val pureconfigVersion = "0.17.8"
val flywayVersion = "11.4.0"
val scalaTestVersion = "3.2.19"
val testcontainersVersion = "0.44.1"

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
  "com.beachape" %% "enumeratum" % "1.7.5"
)

val otherDependencies = Seq(
  "com.github.pureconfig"      %% "pureconfig"                % pureconfigVersion,
  "com.github.pureconfig"      %% "pureconfig-cats-effect"    % pureconfigVersion,
  "org.slf4j"                  % "slf4j-reload4j"             % "2.0.17",
  "com.typesafe.scala-logging" %% "scala-logging"             % "3.9.5",
  "commons-io"                 % "commons-io"                 % "2.18.0",
  "org.flywaydb"               % "flyway-core"                % flywayVersion,
  "org.flywaydb"               % "flyway-database-postgresql" % flywayVersion,
  "com.ibm.icu"                % "icu4j"                      % "77.1"
)

val allCoreDependencies =
  circeDependencies ++
    doobieDependencies ++
    http4sDependencies ++
    enumerationDependencies ++
    otherDependencies

val testingDependencies = Seq(
  "org.scalactic" %% "scalactic" % scalaTestVersion % "test",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "org.scalamock" %% "scalamock" % "6.2.0" % "test"
)

val itDependencies = Seq(
  "org.scalatest" %% "scalatest"                       % scalaTestVersion      % "test",
  "com.dimafeng"  %% "testcontainers-scala-scalatest"  % testcontainersVersion % "test",
  "com.dimafeng"  %% "testcontainers-scala-postgresql" % testcontainersVersion % "test"
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
    libraryDependencies ++= allCoreDependencies ++ testingDependencies
  )

lazy val backend = project
  .in(file("modules/backend"))
  .dependsOn(core, utils)
  .settings(
    name := "flight-database-backend",
    commonSettings,
    libraryDependencies ++= allCoreDependencies ++ testingDependencies
  )

lazy val backendIt = project
  .in(file("modules/backend-it"))
  .dependsOn(backend)
  .settings(
    name           := "flight-database-backend-it",
    publish / skip := true,
    commonSettings,
    libraryDependencies ++= allCoreDependencies ++ itDependencies,
    scalafixConfigSettings(Test)
  )

// TODO: Add frontend module

lazy val root = (project in file("."))
  .aggregate(core, utils, backend, backendIt)
  .settings(
    name := "flight-database",
    commonSettings
  )
