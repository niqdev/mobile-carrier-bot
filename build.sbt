lazy val I = new {
  val organization = "com.github.niqdev"
  val name = "mobile-carrier-bot"
  val version = "0.1"
}

lazy val G = new {
  val scalaScraper = "net.ruippeixotog"
}

lazy val V = new {
  val scala = "2.12.8"

  val scalaScraper = "2.1.0"
}

lazy val lib = (project in file("lib"))
  .settings(
    libraryDependencies ++= Seq(
      G.scalaScraper %% "scala-scraper" % V.scalaScraper
    )
  )

lazy val root = project.in(file("."))
  .settings(
    inThisBuild(List(
      organization := I.organization,
      scalaVersion := V.scala,
      version := I.version,
    )),
    name := I.name
  )
