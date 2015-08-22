import sbt._
import Keys._
import org.xarcher.summer.CustomSettings
import com.typesafe.sbt.SbtGit._

object `slick-summer` extends Build {
  
  val initPrintln = """
 ___  _   _  _ __ ___   _ __ ___    ___  _ __ 
/ __|| | | || '_ ` _ \ | '_ ` _ \  / _ \| '__|
\__ \| |_| || | | | | || | | | | ||  __/| |   
|___/ \__,_||_| |_| |_||_| |_| |_| \___||_|   

"""
  println(initPrintln)

  lazy val `slick-summer` = (project in file("."))
  .settings(CustomSettings.customSettings: _*)
  .settings(
    name := "slick-summer",
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % "3.0.2",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "com.h2database" % "h2" % "1.4.187" % "test",
      "org.slf4j" % "slf4j-simple" % "1.7.12" % "test"
    )
  )

}
