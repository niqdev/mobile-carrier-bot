import sbt._

object Dependencies {

  private[this] lazy val V = new {
    // shared
    val catsCore   = "2.0.0-M4"
    val catsEffect = "1.4.0"
    val refined    = "0.9.9"
    val enumeratum = "1.5.13"
    val log4cats   = "0.3.0"
    val logback    = "1.2.3"
    val silencer   = "1.4.2"

    // app
    val ciris  = "0.12.1"
    val http4s = "0.20.7"
    val circe  = "0.11.1"
    val fs2    = "1.0.5"
    val doobie = "0.7.0"

    // core
    val scalaScraper = "2.1.0"

    // test
    val scalatest  = "3.0.8"
    val scalacheck = "1.14.0"
  }

  lazy val sharedDependencies = Seq(
    "org.typelevel"     %% "cats-core"      % V.catsCore,
    "org.typelevel"     %% "cats-effect"    % V.catsEffect,
    "eu.timepit"        %% "refined"        % V.refined,
    "com.beachape"      %% "enumeratum"     % V.enumeratum,
    "io.chrisdavenport" %% "log4cats-slf4j" % V.log4cats,
    "ch.qos.logback"    % "logback-classic" % V.logback,
    "com.github.ghik"   %% "silencer-lib"   % V.silencer % Provided,
    compilerPlugin("com.github.ghik" %% "silencer-plugin" % V.silencer)
  )

  lazy val appDependencies = Seq(
    "is.cir"       %% "ciris-core"           % V.ciris,
    "is.cir"       %% "ciris-cats"           % V.ciris,
    "is.cir"       %% "ciris-refined"        % V.ciris,
    "is.cir"       %% "ciris-enumeratum"     % V.ciris,
    "org.http4s"   %% "http4s-dsl"           % V.http4s,
    "org.http4s"   %% "http4s-blaze-server"  % V.http4s,
    "org.http4s"   %% "http4s-blaze-client"  % V.http4s,
    "org.http4s"   %% "http4s-circe"         % V.http4s,
    "io.circe"     %% "circe-core"           % V.circe,
    "io.circe"     %% "circe-generic"        % V.circe,
    "io.circe"     %% "circe-generic-extras" % V.circe,
    "io.circe"     %% "circe-parser"         % V.circe,
    "io.circe"     %% "circe-literal"        % V.circe,
    "io.circe"     %% "circe-refined"        % V.circe,
    "co.fs2"       %% "fs2-core"             % V.fs2,
    "org.tpolecat" %% "doobie-core"          % V.doobie,
    "org.tpolecat" %% "doobie-h2"            % V.doobie,
    "org.tpolecat" %% "doobie-hikari"        % V.doobie
  )

  lazy val coreDependencies = Seq(
    "net.ruippeixotog" %% "scala-scraper" % V.scalaScraper
  )

  lazy val testDependencies = Seq(
    "org.scalatest"  %% "scalatest"  % V.scalatest  % Test,
    "org.scalacheck" %% "scalacheck" % V.scalacheck % Test
  )

}
