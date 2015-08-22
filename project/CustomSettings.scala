package org.xarcher.summer

import sbt._
import Keys._

import sbt._
import Keys._

object CustomSettings {

  def customSettings = scalaSettings ++ resolversSettings ++ extAlias

  def scalaSettings =
    Seq(
      scalaVersion := "2.11.7",
      scalacOptions ++= Seq("-feature", "-deprecation"),
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
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
    if (OSName.isWindows)
      Option(windowsGitInitCommandMap)
    else if (OSName.isLinux)
      Option(linuxGitInitCommandMap)
    else None
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