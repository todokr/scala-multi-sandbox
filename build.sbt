import Dependencies._

val circeVersion = "0.12.3"
val doobieVersion = "0.9.0"

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
        "org.postgresql" % "postgresql" % "42.2.16",
        "org.tpolecat"  %% "doobie-core"      % doobieVersion,
        "org.tpolecat"  %% "doobie-postgres"  % doobieVersion,          // Postgres driver 42.2.12 + type mappings.
        "org.tpolecat"  %% "doobie-hikari"    % doobieVersion,
        "org.tpolecat"  %% "doobie-refined"   % doobieVersion,
        "org.tpolecat"  %% "doobie-quill"     % doobieVersion,          // Support for Quill 3.5.1
        "org.tpolecat"  %% "doobie-specs2"    % doobieVersion % "test", // Specs2 support for typechecking statements.
        "org.tpolecat"  %% "doobie-scalatest" % doobieVersion % "test", // ScalaTest support for typechecking statements.
        scalaTest
      ),
      scalacOptions ++= Seq(
        "-Xfatal-warnings", // New lines for each options
        "-deprecation",
        "-unchecked",
        "-language:implicitConversions",
        "-language:higherKinds",
        "-language:existentials",
        "-language:postfixOps",
        "-Ywarn-dead-code",
        "-Ywarn-numeric-widen",
        "-Xfatal-warnings",
        "-deprecation",
        "-Xlint:-unused,_",
        "-deprecation",
        "-Ymacro-annotations",
        "-Xmaxerrs",
        "200",
      )
    )

lazy val processAnnotations = taskKey[Unit]("Process annotations")
processAnnotations := {
  val log = streams.value.log
  log.info("Processing annotations ...")
  val classpath = ((products in Compile).value ++ ((dependencyClasspath in Compile).value.files)) mkString ":"
  val destinationDirectory = (classDirectory in Compile).value
  val processor =
    "picocli.codegen.aot.graalvm.processor.NativeImageConfigGeneratorProcessor"
  val classesToProcess = Seq("sgit.Main") mkString " "
  val command =
    s"javac -cp $classpath -proc:only -processor $processor -XprintRounds -d $destinationDirectory $classesToProcess"
  runCommand(command, "Failed to process annotations.", log)
  log.info("Done processing annotations.")
}
def runCommand(command: String, message: => String, log: Logger) = {
  import scala.sys.process._
  val result = command.!
  if (result != 0) {
    log.error(message)
    sys.error("Failed running command: " + command)
  }
}
packageBin in Compile := (packageBin in Compile dependsOn (processAnnotations in Compile)).value
lazy val sgit = (project in file("sgit"))
  .settings(
    scalaVersion := "2.13.1",
    name := "sgit",
    libraryDependencies ++= Seq(
      "info.picocli" % "picocli" % "4.2.0",
      "info.picocli" % "picocli-codegen" % "4.2.0" % "provided",
      "org.ini4j" % "ini4j" % "0.5.4",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
      "commons-codec" % "commons-codec" % "1.14",
      scalaTest
    ),
    trapExit := false
  )
  .enablePlugins(GraalVMNativeImagePlugin)

lazy val scalametaSample = (project in file("scalameta-sample"))
  .settings(
    scalaVersion := "2.12.8",
    name := "scalameta-sample",
    libraryDependencies ++= Seq(
      "org.scalameta" %% "scalameta" % "4.3.15",
      "org.scalameta" %% "semanticdb" % "4.1.6",
      scalaTest
    ),
    scalacOptions ++= Seq("-Yrangepos")
  )
