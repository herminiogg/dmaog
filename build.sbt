ThisBuild / organization := "com.herminiogarcia"

lazy val dmaog = project
  .in(file("."))
  .settings(
    name := "DMAOG",
    version := "0.1.3",
    scalaVersion := "3.2.0",
    crossScalaVersions := Seq("2.12.17", "2.13.9", "3.2.0"),
    libraryDependencies ++= Seq(
      "org.apache.jena" % "jena-base" % "3.8.0",
      "org.apache.jena" % "jena-core" % "3.8.0",
      "org.apache.jena" % "jena-arq" % "3.8.0",
      "info.picocli" % "picocli" % "4.0.4",
      "be.ugent.rml" % "rmlmapper" % "4.9.0",
      "com.herminiogarcia" %% "shexml" % "0.3.2" excludeAll(ExclusionRule(organization = "org.apache.jena")),
      "org.scalatest" %% "scalatest" % "3.2.9" % "test",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      "ch.qos.logback" % "logback-classic" % "1.3.5"
    ),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  )







