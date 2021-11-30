organization := "com.herminiogarcia"

name := "DMAOG"

version := "0.1"

scalaVersion := "2.12.4"

idePackagePrefix := Some("com.herminiogarcia")

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.github.herminiogg" % "ShExML" % "v0.2.7"

libraryDependencies += "org.apache.jena" % "apache-jena-libs" % "3.17.0" pomOnly()

libraryDependencies += "org.apache.jena" % "jena-arq" % "3.17.0"

libraryDependencies += "info.picocli" % "picocli" % "4.0.4"
