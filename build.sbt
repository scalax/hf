import Keys._

import sbt._
import scala.language.reflectiveCalls
/*val OSName = new {
  val OS = System.getProperty("os.name").toLowerCase
  def isLinux = OS.indexOf("linux") >= 0
  def isMacOS = OS.indexOf("mac") >= 0 && OS.indexOf("os") > 0 && OS.indexOf("x") < 0
  def isMacOSX = OS.indexOf("mac") >= 0 && OS.indexOf("os") > 0 && OS.indexOf("x") > 0
  def isWindows = OS.indexOf("windows") >= 0
  def isOS2 = OS.indexOf("os/2") >= 0
  def isSolaris = OS.indexOf("solaris") >= 0
  def isSunOS = OS.indexOf("sunos") >= 0
  def isMPEiX = OS.indexOf("mpe/ix") >= 0
  def isHPUX = OS.indexOf("hp-ux") >= 0
  def isAix = OS.indexOf("aix") >= 0
  def isOS390 = OS.indexOf("os/390") >= 0
  def isFreeBSD = OS.indexOf("freebsd") >= 0
  def isIrix = OS.indexOf("irix") >= 0
  def isDigitalUnix = OS.indexOf("digital") >= 0 && OS.indexOf("unix") > 0
  def isNetWare = OS.indexOf("netware") >= 0
  def isOSF1 = OS.indexOf("osf1") >= 0
  def isOpenVMS = OS.indexOf("openvms") >= 0
}*/
val CustomSettings = new {

  def customSettings = scalaSettings ++ resolversSettings ++ extAlias

  def scalaSettings =
    Seq(
      scalaVersion := "2.11.8",
      scalacOptions ++= Seq("-feature", "-deprecation"),
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    )

  def resolversSettings =
    Seq(
      resolvers ++= Seq(
        "mavenRepoJX" at "http://repo1.maven.org/maven2/",
        "bintray/non" at "http://dl.bintray.com/non/maven",
        "aa" at "https://oss.sonatype.org/service/local/repositories/snapshots/content/",
        Resolver.url("typesafe-ivy", url("http://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns)
      ),
      externalResolvers := Resolver.withDefaultResolvers(resolvers.value, mavenCentral = false)
    )

  def extAliasInfo = List(
    Option("xeclipse" -> "eclipse with-source=true skip-parents=false"),
    if (/*OSName.isWindows*/scala.util.Properties.isWin)
      Option(windowsGitInitCommandMap)
    else//if (OSName.isLinux)
      Option(linuxGitInitCommandMap)
  )

  def extAlias = extAliasInfo.collect { case Some(s) => s }
    .foldLeft(List.empty[Def.Setting[_]]){ (s, t) => s ++ addCommandAlias(t._1, t._2) }

  //git init command
  val windowsGitInitCommandMap = "windowsGitInit" ->
    """|;
      |git config --global i18n.commitencoding utf-8;
      |git config --global i18n.logoutputencoding gbk;
      |git config --global core.autocrlf true;
      |git config core.editor \"extras/npp.6.5.1/startNote.bat\"
    """.stripMargin

  val linuxGitInitCommandMap = "linuxGitInit" ->
    """|;
      |git config --global i18n.commitencoding utf-8;
      |git config --global i18n.logoutputencoding utf-8;
      |git config --global core.autocrlf true;
      |git config core.editor gedit
    """.stripMargin

}

val initPrintln = {
  println("""
   ___  _   _  _ __ ___   _ __ ___    ___  _ __
  / __|| | | || '_ ` _ \ | '_ ` _ \  / _ \| '__|
  \__ \| |_| || | | | | || | | | | ||  __/| |
  |___/ \__,_||_| |_| |_||_| |_| |_| \___||_|

  """
  )
}

CustomSettings.customSettings

name := "slick-summer"
libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.2.0-M1",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.h2database" % "h2" % "1.4.187" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.12" % "test"
)