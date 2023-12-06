val scala213 = "2.13.12"
val scala3 = "3.3.1"
val scalaVersions = Seq(scala3, scala213)

name := "tagless-mid"
version := "0.1.0-SNAPSHOT"
scalaVersion := scala213
crossScalaVersions := Seq(scala213, scala3)
organization := "io.github.pomadchin"
scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-language:higherKinds",
  "-language:postfixOps",
  "-language:existentials",
  "-feature"
)

scalacOptions ++= when(scalaBinaryVersion.value == "2.13")("-Ymacro-annotations", "-Xsource:3")
scalacOptions ++= (scalaBinaryVersion.value match {
  case "3" => List("-Ykind-projector:underscores")
  case _   => List("-Xsource:3", "-P:kind-projector:underscore-placeholders")
})

resolvers ++= Resolver.sonatypeOssRepos("releases") ++ Resolver.sonatypeOssRepos("snapshots")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-tagless-core" % "0.15.0",
  "org.typelevel" %% "cats-effect" % "3.5.2" % Test,
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test,
  "org.scalatest" %% "scalatest" % "3.2.17" % Test
)

libraryDependencies ++= when(scalaBinaryVersion.value.startsWith("2"))(
  compilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)
)

def when[A](condition: Boolean)(values: A*): Seq[A] =
  if (condition) values else Nil
