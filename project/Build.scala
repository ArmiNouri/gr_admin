import sbt._
import Keys._
import play.Project._
//import PlayProject._

object ApplicationBuild extends Build {

  val appName         = "gr_admin"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    //other deps
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "org.apache.poi" % "poi" % "3.7",
    "org.apache.poi" % "poi-ooxml" % "3.7"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(defaultScalaSettings:_*).settings(
    //resolvers ++= Seq("repo.codahale.com" at "http://repo.codahale.com", "spray" at "http://repo.spray.io/")
    scalacOptions ++=Seq("-unchecked", "-deprecation", "-Xlint", "-language:_", "-target:jvm-1.6", "-encoding", "UTF-8"),
    javacOptions in Compile ++= Seq("-source", "1.6",  "-target", "1.6"),
    javacOptions in doc ++= Seq("-source", "1.6")
  ).dependsOn(RootProject( uri("git://github.com/freekh/play-slick.git") ))

}
