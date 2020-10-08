addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.11")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.6.1")
addCompilerPlugin(
  "org.scalameta" % "semanticdb-scalac" % "4.3.15" cross CrossVersion.full
)
