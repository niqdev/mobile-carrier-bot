import sbt._

object Dependencies {

  private[this] lazy val V = new {
    // shared
    val catsCore = "1.6.0"
    val catsEffect = "1.2.0"
    val refined = "0.9.5"
    val enumeratum = "1.5.13"
    val log4cats = "0.3.0"
    val logback = "1.2.3"
    val silencer = "1.3.3"

    // app
    val ciris = "0.12.1"
    val http4s = "0.20.0-SNAPSHOT"

    // core
    val scalaScraper = "2.1.0"

    // test
    val scalatest = "3.0.5"
    val scalacheck = "1.14.0"
  }

  lazy val sharedDependencies = Seq(
    "org.typelevel" %% "cats-core" % V.catsCore,
    "org.typelevel" %% "cats-effect" % V.catsEffect,
    "eu.timepit" %% "refined" % V.refined,
    "com.beachape" %% "enumeratum" % V.enumeratum,
    "io.chrisdavenport" %% "log4cats-slf4j" % V.log4cats,
    "ch.qos.logback" % "logback-classic" % V.logback,
    "com.github.ghik" %% "silencer-lib" % V.silencer % Provided,
    compilerPlugin("com.github.ghik" %% "silencer-plugin" % V.silencer)
  )

  lazy val appDependencies = Seq(
    "is.cir" %% "ciris-core" % V.ciris,
    "is.cir" %% "ciris-cats" % V.ciris,
    "is.cir" %% "ciris-refined" % V.ciris,
    "org.http4s" %% "http4s-dsl" % V.http4s,
    "org.http4s" %% "http4s-blaze-server" % V.http4s,
    "org.http4s" %% "http4s-blaze-client" % V.http4s,
    "org.http4s" %% "http4s-circe" % V.http4s
  )

  lazy val coreDependencies = Seq(
    "net.ruippeixotog" %% "scala-scraper" % V.scalaScraper
  )

  lazy val testDependencies = Seq(
    "org.scalatest" %% "scalatest" % V.scalatest % Test,
    "org.scalacheck" %% "scalacheck" % V.scalacheck % Test
  )

}
