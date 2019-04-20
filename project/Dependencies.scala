import sbt._

object Dependencies {

  private[this] lazy val V = new {
    val catsCore = "1.6.0"
    val catsEffect = "1.2.0"
    val refined = "0.9.5"
    val enumeratum = "1.5.13"

    val log4cats = "0.3.0"
    val logback = "1.2.3"

    val ciris = "0.12.1"

    val http4s = "0.20.0-SNAPSHOT"

    val scalaScraper = "2.1.0"
  }

  lazy val libDependencies = Seq(
    "org.typelevel" %% "cats-core" % V.catsCore,
    "org.typelevel" %% "cats-effect" % V.catsEffect,
    "eu.timepit" %% "refined" % V.refined,
    "com.beachape" %% "enumeratum" % V.enumeratum,

    "io.chrisdavenport" %% "log4cats-slf4j" % V.log4cats,
    "ch.qos.logback" % "logback-classic" % V.logback,

    "is.cir" %% "ciris-core" % V.ciris,
    "is.cir" %% "ciris-cats" % V.ciris,
    "is.cir" %% "ciris-refined" % V.ciris,

    "org.http4s" %% "http4s-dsl" % V.http4s,
    "org.http4s" %% "http4s-blaze-server" % V.http4s,
    "org.http4s" %% "http4s-blaze-client" % V.http4s,

    "net.ruippeixotog" %% "scala-scraper" % V.scalaScraper
  )

  lazy val testDependencies = Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
  )

}
