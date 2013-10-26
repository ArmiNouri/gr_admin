name := "gr_admin"

version := "0.1-SNAPSHOT"


scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)


libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  //"com.typesafe.slick" %% "slick" % "1.0.1",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  //"com.h2database" % "h2" % "1.3.166",
  //"mysql" % "mysql-connector-java" % "5.1.25",
  "org.apache.poi" % "poi" % "3.7",
  "org.apache.poi" % "poi-ooxml" % "3.7"
)     

play.Project.playScalaSettings
