package project

import sbt._

object Dependencies {

  lazy val doobieVersion = "0.9.0"
  lazy val doobieLibs: Seq[ModuleID] = Seq(
    "org.postgresql" % "postgresql" % "42.2.16",
    "org.tpolecat"  %% "doobie-core"      % doobieVersion,
    "org.tpolecat"  %% "doobie-postgres"  % doobieVersion,          // Postgres driver 42.2.12 + type mappings.
    "org.tpolecat" %% "doobie-hikari"     % doobieVersion,
    "org.tpolecat"  %% "doobie-refined"   % doobieVersion,
    "org.tpolecat"  %% "doobie-quill"     % doobieVersion,          // Support for Quill 3.5.1
    "org.tpolecat"  %% "doobie-specs2"    % doobieVersion % "test", // Specs2 support for typechecking statements.
    "org.tpolecat"  %% "doobie-scalatest" % doobieVersion % "test"  // ScalaTest support for typechecking statements.
  )
}
