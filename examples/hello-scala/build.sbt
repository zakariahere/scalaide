ThisBuild / scalaVersion := "3.3.6"
ThisBuild / organization := "dev.scalaide.example"
ThisBuild / version := "0.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "hello-scala",
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.4" % Test
  )
