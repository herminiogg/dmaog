organization := "com.herminiogarcia"

name := "DMAOG"

version := "0.1"

scalaVersion := "2.12.4"

idePackagePrefix := Some("com.herminiogarcia")

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.github.herminiogg" % "ShExML" % "v0.2.7"

libraryDependencies += "org.apache.jena" % "apache-jena-libs" % "3.8.0" pomOnly()

libraryDependencies += "org.apache.jena" % "jena-arq" % "3.8.0"

libraryDependencies += "info.picocli" % "picocli" % "4.0.4"

libraryDependencies += "be.ugent.rml" % "rmlmapper" % "4.9.0"

// Allows to avoid the conflicts between ShExML and RML jena versions
dependencyOverrides += "org.apache.jena" % "apache-jena-libs" % "3.8.0"
