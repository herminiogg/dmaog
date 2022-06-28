organization := "com.herminiogarcia"

name := "DMAOG"

version := "0.1.1"

scalaVersion := "2.12.4"

libraryDependencies += "com.herminiogarcia" %% "shexml" % "0.3.0"

libraryDependencies += "org.apache.jena" % "apache-jena-libs" % "3.8.0" pomOnly()

libraryDependencies += "org.apache.jena" % "jena-arq" % "3.8.0"

libraryDependencies += "info.picocli" % "picocli" % "4.0.4"

libraryDependencies += "be.ugent.rml" % "rmlmapper" % "4.9.0"

libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.2.11" % "test"

// Allows to avoid the conflicts between ShExML and RML jena versions
dependencyOverrides += "org.apache.jena" % "apache-jena-libs" % "3.8.0"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
