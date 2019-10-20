lazy val info = new {
  val maintainer   = "niqdev"
  val organization = "com.github.niqdev"
  val name         = "mobile-carrier-bot"
  val scalaVersion = "2.12.10"
}

lazy val versions = new {
  // shared
  val catsCore   = "2.0.0"
  val catsEffect = "2.0.0"
  val refined    = "0.9.10"
  val enumeratum = "1.5.13"
  val log4cats   = "1.0.1"
  val logback    = "1.2.3"
  val silencer   = "1.4.2"

  // app
  val ciris  = "0.12.1"
  val http4s = "0.20.11"
  val circe  = "0.12.2"
  val fs2    = "1.0.5"
  val doobie = "0.7.1"

  // core
  val scalaScraper = "2.2.0"

  // test
  val scalatest  = "3.0.8"
  val scalacheck = "1.14.2"
}

lazy val dependencies = new {
  lazy val shared = Seq(
    "org.typelevel"     %% "cats-core"      % versions.catsCore,
    "org.typelevel"     %% "cats-effect"    % versions.catsEffect,
    "eu.timepit"        %% "refined"        % versions.refined,
    "com.beachape"      %% "enumeratum"     % versions.enumeratum,
    "io.chrisdavenport" %% "log4cats-slf4j" % versions.log4cats,
    "ch.qos.logback"    % "logback-classic" % versions.logback,
    "com.github.ghik"   %% "silencer-lib"   % versions.silencer % Provided,
    compilerPlugin("com.github.ghik" %% "silencer-plugin" % versions.silencer)
  )

  lazy val app = Seq(
    "is.cir"       %% "ciris-core"           % versions.ciris,
    "is.cir"       %% "ciris-cats"           % versions.ciris,
    "is.cir"       %% "ciris-refined"        % versions.ciris,
    "is.cir"       %% "ciris-enumeratum"     % versions.ciris,
    "org.http4s"   %% "http4s-dsl"           % versions.http4s,
    "org.http4s"   %% "http4s-blaze-server"  % versions.http4s,
    "org.http4s"   %% "http4s-blaze-client"  % versions.http4s,
    "org.http4s"   %% "http4s-circe"         % versions.http4s,
    "io.circe"     %% "circe-core"           % versions.circe,
    "io.circe"     %% "circe-generic"        % versions.circe,
    "io.circe"     %% "circe-generic-extras" % versions.circe,
    "io.circe"     %% "circe-parser"         % versions.circe,
    "io.circe"     %% "circe-literal"        % versions.circe,
    "io.circe"     %% "circe-refined"        % versions.circe,
    "co.fs2"       %% "fs2-core"             % versions.fs2,
    "org.tpolecat" %% "doobie-core"          % versions.doobie,
    "org.tpolecat" %% "doobie-h2"            % versions.doobie,
    "org.tpolecat" %% "doobie-hikari"        % versions.doobie
  )

  lazy val core = Seq(
    "net.ruippeixotog" %% "scala-scraper" % versions.scalaScraper
  )

  lazy val test = Seq(
    "org.scalatest"  %% "scalatest"  % versions.scalatest  % Test,
    "org.scalacheck" %% "scalacheck" % versions.scalacheck % Test
  )
}

lazy val commonSettings = Seq(
  organization := info.organization,
  scalaVersion := info.scalaVersion,
  scalacOptions ++= Seq(
    "-encoding",
    "utf8",
    "-Xfatal-warnings",
    "-deprecation",
    "-unchecked",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-language:postfixOps",
    "-Ypartial-unification"
  )
)

lazy val buildInfoSettings = Seq(
  buildInfoKeys    := Seq[BuildInfoKey]("name" -> info.name, version, scalaVersion, sbtVersion),
  buildInfoPackage := info.organization,
  buildInfoOptions += BuildInfoOption.BuildTime
)

lazy val dockerSettings = Seq(
  maintainer in Docker := info.maintainer,
  // FIXME upgrade to openjdk:11
  dockerBaseImage in Docker  := "openjdk:8-jre-alpine3.8",
  dockerRepository in Docker := Some(info.maintainer),
  // FIXME Seq(sys.enversions.get("HTTP_PORT").getOrElse(8080))
  dockerExposedPorts := Seq(8080)
  // FIXME java.io.FileNotFoundException: log/output.log (Permission denied)
  //dockerExposedVolumes := Seq("/opt/docker/log")
)

lazy val compilerSettings = Seq(
  // required by circe-generic-extras
  addCompilerPlugin(
    "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
  )
)

lazy val app = (project in file("modules/app"))
  .dependsOn(core % "compile->compile;test->test") // makes BaseSpec accessible
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(buildInfoSettings)
  .settings(dockerSettings)
  .settings(compilerSettings)
  .settings(
    name := s"${info.name}-app",
    libraryDependencies ++= (dependencies.app ++ dependencies.shared ++ dependencies.test)
      .map(_.withSources)
      .map(_.withJavadoc)
  )

lazy val core = (project in file("modules/core"))
  .settings(commonSettings)
  .settings(
    name := s"${info.name}-core",
    libraryDependencies ++= (dependencies.core ++ dependencies.shared ++ dependencies.test)
      .map(_.withSources)
      .map(_.withJavadoc)
  )

lazy val root = project
  .in(file("."))
  .aggregate(app, core)
  .settings(
    name := info.name,
    addCommandAlias("checkFormat", ";scalafmtCheckAll;scalafmtSbtCheck"),
    addCommandAlias("format", ";scalafmtAll;scalafmtSbt"),
    addCommandAlias("update", ";dependencyUpdates;reload plugins;dependencyUpdates;reload return"),
    addCommandAlias("build", ";checkFormat;clean;test")
  )
