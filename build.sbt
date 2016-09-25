import scala.language.reflectiveCalls

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

name := "hf"
libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.2.0-M1",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.h2database" % "h2" % "1.4.187" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.12" % "test"
)