organization in ThisBuild := "com.herminiogarcia"
organizationName in ThisBuild := "herminiogarcia"
organizationHomepage in ThisBuild := Some(url("https://herminiogarcia.com"))

scmInfo in ThisBuild := Some(
  ScmInfo(
    url("https://github.com/herminiogg/dmaog"),
    "scm:git@github.com:herminiogg/dmaog.git"
  )
)

developers in ThisBuild := List(
  Developer(
    id    = "herminiogg",
    name  = "Herminio Garcia Gonzalez",
    email = "herminio@herminiogarcia.com",
    url   = url("https://herminiogarcia.com")
  )
)

description in ThisBuild := "Data Mapping Access Objects Generator (DMAOG)"
licenses in ThisBuild := List("MIT License" -> new URL("https://opensource.org/licenses/MIT"))
homepage in ThisBuild := Some(url("https://github.com/herminiogg/dmaog"))

// Remove all additional repository other than Maven Central from POM
pomIncludeRepository in ThisBuild := { _ => false }

publishTo in ThisBuild := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle in ThisBuild := true
