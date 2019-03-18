import sbt._

object Dependencies {

  private[this] lazy val G = new {
    val typelevel = "org.typelevel"
    val scalaScraper = "net.ruippeixotog"
  }

  private[this] lazy val V = new {
    val catsCore = "1.6.0"
    val catsEffect = "1.2.0"

    val scalaScraper = "2.1.0"
  }

  lazy val libDependencies = Seq(
    G.typelevel %% "cats-core" % "1.6.0",
    G.typelevel %% "cats-effect" % "1.2.0",

    G.scalaScraper %% "scala-scraper" % V.scalaScraper
  )

}
