scalaVersion := "2.12.5"
scalacOptions ++= Seq("-feature", "-deprecation")
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

name := "hf"
libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.2.3",
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.h2database" % "h2" % "1.4.187" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.12" % "test"
)