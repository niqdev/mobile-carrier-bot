lazy val I = new {
  val organization = "com.github.niqdev"
  val name = "mobile-carrier-bot"
  val version = "0.1"
}

lazy val V = new {
  val scala = "2.12.8"
}

lazy val lib = (project in file("lib"))
  .settings()

lazy val root = project.in(file("."))
  .settings(
    inThisBuild(List(
      organization := I.organization,
      scalaVersion := V.scala,
      version := I.version,
    )),
    name := I.name
  )
