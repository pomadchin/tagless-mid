val scala213 = "2.13.12"
val scala3 = "3.3.1"
val scalaVersions = Seq(scala3, scala213)

lazy val commonSettings = Seq(
  scalaVersion := scalaVersions.head,
  crossScalaVersions := scalaVersions,
  organization := "io.github.pomadchin",
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:existentials",
    "-feature"
  ),
  scalacOptions ++= when(scalaBinaryVersion.value == "2.13")("-Ymacro-annotations", "-Xsource:3"),
  scalacOptions ++= (scalaBinaryVersion.value match {
    case "3" => List("-Ykind-projector:underscores")
    case _   => List("-Xsource:3", "-P:kind-projector:underscore-placeholders")
  }),
  description := "ToFu Mid as a separate library",
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("https://github.com/pomadchin/tagless-mid")),
  scmInfo := Some(
    ScmInfo(url("https://github.com/pomadchin/tagless-mid"), "scm:git:git@github.com:pomadchin/tagless-mid.git")
  ),
  versionScheme := Some("semver-spec"),
  Test / publishArtifact := false,
  Test / fork := true,
  developers := List(
    Developer(
      "pomadchin",
      "Grigory Pomadchin",
      "@pomadchin",
      url("https://github.com/pomadchin")
    )
  ),
  // sonatype settings
  sonatypeProfileName := "io.github.pomadchin",
  sonatypeCredentialHost := "s01.oss.sonatype.org",
  sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
  // resolvers
  resolvers ++= Resolver.sonatypeOssRepos("releases") ++ Resolver.sonatypeOssRepos("snapshots"),
  // compiler plugins
  libraryDependencies ++= when(scalaBinaryVersion.value.startsWith("2"))(
    compilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)
  )
)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(name := "tagless-mid")
  .settings(publish := {}, publishLocal := {})
  .aggregate(core)

lazy val core = project
  .settings(commonSettings)
  .settings(name := "tagless-mid-core")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-tagless-core" % "0.15.0",
      "org.typelevel" %% "cats-effect" % "3.5.2" % Test,
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test,
      "org.scalatest" %% "scalatest" % "3.2.17" % Test
    )
  )

def when[A](condition: Boolean)(values: A*): Seq[A] =
  if (condition) values else Nil
