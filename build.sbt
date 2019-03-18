import Dependencies.libDependencies

lazy val lib = (project in file("lib"))
  .settings(
    libraryDependencies ++= libDependencies
      .map(_.withSources).map(_.withJavadoc)
  )

lazy val root = project.in(file("."))
  .settings(
    inThisBuild(List(
      organization := "com.github.niqdev",
      scalaVersion := "2.12.8",
      version := "0.1",
    )),
    name := "mobile-carrier-bot"
  )
