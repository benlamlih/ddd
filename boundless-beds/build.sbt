ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

name             := "BoundlessBeds"
idePackagePrefix := Some("net.benlamlih")

val http4sVersion = "1.0.0-M40"

libraryDependencies ++= Seq(
  "eu.timepit"        %% "refined"             % "0.11.0",
  "org.tpolecat"      %% "doobie-postgres"     % "1.0.0-RC4",
  "org.tpolecat"      %% "doobie-hikari"       % "1.0.0-RC4",
  "org.tpolecat"      %% "doobie-refined"      % "1.0.0-RC4",
  "org.http4s"        %% "http4s-circe"        % http4sVersion,
  "io.circe"          %% "circe-generic"       % "0.14.6",
  "io.circe"          %% "circe-refined"       % "0.15.0-M1",
  "io.circe"          %% "circe-literal"       % "0.14.6",
  "io.circe"          %% "circe-parser"        % "0.14.6",
  "org.http4s"        %% "http4s-dsl"          % http4sVersion,
  "org.http4s"        %% "http4s-ember-server" % http4sVersion,
  "org.http4s"        %% "http4s-ember-client" % http4sVersion,
  "org.typelevel"     %% "log4cats-slf4j"      % "2.5.0",
  "ch.qos.logback"     % "logback-classic"     % "1.4.7",
  "org.postgresql"     % "postgresql"          % "42.5.4",
  "org.scalatestplus" %% "mockito-3-4"         % "3.2.10.0" % Test,
  "org.scalatest"     %% "scalatest"           % "3.2.15"   % Test
)

enablePlugins(FlywayPlugin)
version := "0.0.1"
name    := "flyway-sbt-test1"

flywayUrl      := "jdbc:postgresql://localhost:5432/boundless-beds"
flywayUser     := "user"
flywayPassword := "password"
flywayLocations += "db/migration"

flywayMigrate := flywayMigrate.value
