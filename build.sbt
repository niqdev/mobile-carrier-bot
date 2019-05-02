import Dependencies.{ appDependencies, coreDependencies, sharedDependencies, testDependencies }

lazy val I = new {
  val maintainer   = "niqdev"
  val organization = "com.github.niqdev"
  val name         = "mobile-carrier-bot"
  val scalaVersion = "2.12.8"
  // TODO sbt-dynver
  val version = "0.1"
}

lazy val buildInfoSettings = Seq(
  buildInfoKeys    := Seq[BuildInfoKey]("name" -> I.name, version, scalaVersion, sbtVersion),
  buildInfoPackage := I.organization,
  buildInfoOptions += BuildInfoOption.BuildTime
)

lazy val dockerSettings = Seq(
  maintainer in Docker := I.maintainer,
  // FIXME upgrade to openjdk:11
  dockerBaseImage in Docker  := "openjdk:8-jre-alpine3.8",
  dockerRepository in Docker := Some(I.maintainer),
  // FIXME Seq(sys.env.get("HTTP_PORT").getOrElse(8080))
  dockerExposedPorts := Seq(8080)
  // FIXME java.io.FileNotFoundException: log/output.log (Permission denied)
  //dockerExposedVolumes := Seq("/opt/docker/log")
)

lazy val app = (project in file("app"))
  // makes BaseSpec accessible
  .dependsOn(core % "compile->compile;test->test")
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(buildInfoSettings)
  .settings(dockerSettings)
  .settings(
    name := s"${I.name}-app",
    libraryDependencies ++= (appDependencies ++ sharedDependencies ++ testDependencies)
      .map(_.withSources)
      .map(_.withJavadoc)
  )

lazy val core = (project in file("core"))
  .settings(
    name := s"${I.name}-core",
    libraryDependencies ++= (coreDependencies ++ sharedDependencies ++ testDependencies)
      .map(_.withSources)
      .map(_.withJavadoc)
  )

lazy val root = project
  .in(file("."))
  .aggregate(app, core)
  .settings(
    name := I.name,
    inThisBuild(
      List(
        organization := I.organization,
        scalaVersion := I.scalaVersion,
        version      := I.version
      )
    ),
    addCommandAlias("checkFormat", ";scalafmtCheckAll;scalafmtSbtCheck"),
    addCommandAlias("format", ";scalafmtAll;scalafmtSbt"),
    addCommandAlias("update", ";dependencyUpdates;reload plugins;dependencyUpdates;reload return"),
    addCommandAlias("build", ";checkFormat;clean;test")
  )
