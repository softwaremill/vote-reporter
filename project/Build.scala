import sbt._
import sbt.Keys._
import sbtassembly.Plugin.assemblySettings
import sbtassembly.Plugin.AssemblyKeys._


object Resolvers {
  val customResolvers = Seq(
    "SoftwareMill Public Releases" at "https://nexus.softwaremill.com/content/repositories/releases/",
    "SoftwareMill Public Snapshots" at "https://nexus.softwaremill.com/content/repositories/snapshots/",
    "spray" at "http://repo.spray.io/"
  )
}

object Dependencies {

  private val slf4jVersion = "1.7.6"
  val logging = Seq(
    "org.slf4j" % "slf4j-api" % slf4jVersion,
    "org.slf4j" % "log4j-over-slf4j" % slf4jVersion,
    "ch.qos.logback" % "logback-classic" % "1.1.1",
    "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"
  )

  val macwireVersion = "0.6"
  val macwire = Seq(
    "com.softwaremill.macwire" %% "macros" % macwireVersion,
    "com.softwaremill.macwire" %% "runtime" % macwireVersion
  )

  val json4sVersion = "3.2.10"
  val json4s = "org.json4s" %% "json4s-jackson" % json4sVersion
  val json4sExt = "org.json4s" %% "json4s-ext" % json4sVersion

  val sprayVersion = "1.3.1-20140423"
  val spray = Seq(
    "io.spray" %% "spray-client" % sprayVersion
  )

  val httpStack = Seq(
    json4s,
    json4sExt
  ) ++ spray

  val gardenVersion = "0.0.20-SNAPSHOT"
  val garden = Seq("garden-web", "lawn", "shrubs").map("com.softwaremill.thegarden" %% _ % gardenVersion)

  val akkaVersion = "2.3.4"
  val akkaActors = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
  val akka = Seq(akkaActors, akkaTestKit)

  val pi4j = "com.pi4j" % "pi4j-core" % "0.0.5"

  lazy val commonDependencies = logging ++ macwire ++ httpStack ++ akka ++ garden ++
    Seq(pi4j)
}

object VoteReporterBuild extends Build {

  import Dependencies._

  override val settings = super.settings ++ Seq(
    name := "vote-reporter",
    version := "1.4",
    scalaVersion := "2.11.1",
    scalacOptions in GlobalScope in Compile := Seq("-unchecked", "-deprecation", "-feature"),
    scalacOptions in GlobalScope in Test := Seq("-unchecked", "-deprecation", "-feature"),
    organization := "com.softwaremill.votecounter"
  )

  lazy val slf4jExclusionHack = Seq(
    ivyXML :=
      <dependencies>
        <exclude org="org.slf4j" artifact="slf4j-log4j12"/>
        <exclude org="log4j" artifact="log4j"/>
      </dependencies>
  )

  lazy val commonSettings = Defaults.defaultSettings ++
    Seq(isSnapshot <<= isSnapshot or version(_ endsWith "-SNAPSHOT")) ++ slf4jExclusionHack ++
    Seq(
      resolvers ++= Resolvers.customResolvers
    ) ++
    Seq(
      mainClass in assembly := Some("com.softwaremill.votereporter.Main")
    ) ++ assemblySettings


  lazy val root = Project(
    id = "vote-reporter",
    base = file("."),
    settings = commonSettings
  ).settings(
      libraryDependencies ++= commonDependencies
    )
}
