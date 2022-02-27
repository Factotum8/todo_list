course := "effective-scala"
assignment := "todo"
ThisBuild / scalaVersion := "3.0.0-RC2"

javacOptions ++= Seq("-source", "1.13", "-target", "1.13", "-Xlint")

val DottyVersion = "3.0.0"
val Http4sVersion = "1.0.0-M4"
val CirceVersion = "0.13.0"
val LogbackVersion = "1.2.3"

scalaVersion := DottyVersion

Global / onChangedBuildSource := ReloadOnSourceChanges

libraryDependencies ++= Seq(
  ("org.http4s"     %% "http4s-ember-server" % Http4sVersion).cross(CrossVersion.for3Use2_13),
  ("org.http4s"     %% "http4s-circe"        % Http4sVersion).cross(CrossVersion.for3Use2_13),
  ("org.http4s"     %% "http4s-dsl"          % Http4sVersion).cross(CrossVersion.for3Use2_13),
  ("io.circe"       %% "circe-parser"        % CirceVersion).cross(CrossVersion.for3Use2_13),
  "ch.qos.logback"  %  "logback-classic"     % LogbackVersion,
  "org.scalameta"   %% "munit"               % "0.7.26" % Test,
  "com.novocode"    %  "junit-interface"     % "0.11" % Test,
)

scalacOptions += "-language:implicitConversions"

initialize := {
  val _ = initialize.value // run the previous initialization
  val required = "13"
  val current  = sys.props("java.specification.version")
  assert(current == required, s"Unsupported JDK: java.specification.version $current != $required")
}

//javacOptions ++= Seq("-source", "1.13", "-target", "1.13", "-Xlint")

//scalacOptions += "-target:jvm-1.13"
//javacOptions ++= Seq("-source", "1.13", "-target", "1.13")
//javacOptions ++= Seq("-source", "1.13", "-target", "1.13")
//javacOptions ++= Seq("-source", "1.13")
