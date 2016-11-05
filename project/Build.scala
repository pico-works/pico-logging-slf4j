import sbt.Keys._
import sbt._

object Build extends sbt.Build {  
  val pico_logging              = "org.pico"        %%  "pico-logging"              % "4.0.1"

  val slf4j_api                 = "org.slf4j"       %   "slf4j-api"                 % "1.7.6"

  val specs2_core               = "org.specs2"      %%  "specs2-core"               % "3.8.6"

  implicit class ProjectOps(self: Project) {
    def standard(theDescription: String) = {
      self
          .settings(scalacOptions in Test ++= Seq("-Yrangepos"))
          .settings(publishTo := Some("Releases" at "s3://dl.john-ky.io/maven/releases"))
          .settings(description := theDescription)
          .settings(isSnapshot := true)
          .settings(resolvers += Resolver.sonatypeRepo("releases"))
          .settings(addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.3" cross CrossVersion.binary))
    }

    def notPublished = self.settings(publish := {}).settings(publishArtifact := false)

    def libs(modules: ModuleID*) = self.settings(libraryDependencies ++= modules)

    def testLibs(modules: ModuleID*) = self.libs(modules.map(_ % "test"): _*)
  }

  lazy val `pico-fake` = Project(id = "pico-fake", base = file("pico-fake"))
      .standard("Fake project").notPublished
      .testLibs(specs2_core)

  lazy val `pico-logging-slf4j` = Project(id = "pico-logging-slf4j", base = file("pico-logging-slf4j"))
      .standard("Tiny logging library")
      .libs(pico_logging, slf4j_api)
      .testLibs(specs2_core)

  lazy val all = Project(id = "pico-logging-slf4j-project", base = file("."))
      .notPublished
      .aggregate(`pico-logging-slf4j`, `pico-fake`)
}
