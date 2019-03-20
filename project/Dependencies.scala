import sbt._

object Dependencies {

  private[this] lazy val G = new {
    val typelevel = "org.typelevel"
    val scalaScraper = "net.ruippeixotog"
    val log4cats = "io.chrisdavenport"
    val logback = "ch.qos.logback"
  }

  private[this] lazy val V = new {
    val catsCore = "1.6.0"
    val catsEffect = "1.2.0"
    val log4cats = "0.3.0"
    val logback = "1.2.3"

    val scalaScraper = "2.1.0"
  }

  lazy val libDependencies = Seq(
    G.typelevel %% "cats-core" % V.catsCore,
    G.typelevel %% "cats-effect" % V.catsEffect,
    G.log4cats %% "log4cats-slf4j" % V.log4cats,
    G.logback % "logback-classic" % V.logback,

    G.scalaScraper %% "scala-scraper" % V.scalaScraper
  )

}
