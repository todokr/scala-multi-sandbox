import Dependencies._

val circeVersion = "0.12.3"

lazy val refinedCirceWithNewtype =
  (project in file("refined_circe_with_newtype"))
    .settings(
      scalaVersion := "2.13.1",
      name := "Refined Circe with Newtype",
      libraryDependencies ++= Seq(
        compilerPlugin(
          "org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full
        ),
        "io.circe" %% "circe-core" % circeVersion,
        "io.circe" %% "circe-generic" % circeVersion,
        "io.circe" %% "circe-parser" % circeVersion,
        "io.circe" %% "circe-refined" % circeVersion,
        "com.softwaremill.common" %% "tagging" % "2.2.1",
        "eu.timepit" %% "refined" % "0.9.13",
        "io.estatico" %% "newtype" % "0.4.3",
        scalaTest
      ),
      scalacOptions ++= Seq(
        "-encoding",
        "utf8", // Option and arguments on same line
        "-Xfatal-warnings", // New lines for each options
        "-deprecation",
        "-unchecked",
        "-language:implicitConversions",
        "-language:higherKinds",
        "-language:existentials",
        "-language:postfixOps",
        "-Ywarn-dead-code",
        "-Ywarn-numeric-widen",
        "-Ywarn-value-discard",
        "-Xfatal-warnings",
        "-deprecation",
        "-Xlint:-unused,_",
        "-deprecation",
        "-Ymacro-annotations",
        "-Xmaxerrs",
        "200"
      )
    )
