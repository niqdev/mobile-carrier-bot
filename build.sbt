import Dependencies.{appDependencies, coreDependencies, sharedDependencies, testDependencies}

lazy val app = (project in file("app"))
  .dependsOn(core)
  .settings(
    resolvers += Resolver.sonatypeRepo("snapshots"),

    libraryDependencies ++= (appDependencies ++ sharedDependencies ++ testDependencies)
      .map(_.withSources).map(_.withJavadoc)
  )

lazy val core = (project in file("core"))
  .settings(
    libraryDependencies ++= (coreDependencies ++ sharedDependencies ++ testDependencies)
      .map(_.withSources).map(_.withJavadoc)
  )

lazy val root = project.in(file("."))
  .aggregate(app, core)
  .settings(
    inThisBuild(List(
      organization := "com.github.niqdev",
      scalaVersion := "2.12.8",
      version := "0.1",
    )),
    name := "mobile-carrier-bot"
  )
