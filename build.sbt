ThisBuild / organization := "com.herminiogarcia"

lazy val dmaog = project
  .in(file("."))
  .settings(
    name := "DMAOG",
    version := "0.1.1",
    scalaVersion := "3.2.0",
    crossScalaVersions := Seq("2.12.17", "2.13.9", "3.2.0"),
    libraryDependencies ++= Seq(
      "com.herminiogarcia" %% "shexml" % "0.3.2",
      "org.apache.jena" % "jena-base" % "3.17.0",
      "org.apache.jena" % "jena-core" % "3.17.0",
      "org.apache.jena" % "jena-arq" % "3.17.0",
      "info.picocli" % "picocli" % "4.0.4",
      "be.ugent.rml" % "rmlmapper" % "4.9.0",
      "org.scalatest" %% "scalatest" % "3.2.9" % "test"
    ),
    // Allows to avoid the conflicts between ShExML and RML jena versions
    dependencyOverrides ++= Seq(
      "org.apache.jena" % "apache-jena-libs" % "3.8.0"
    ),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  )







