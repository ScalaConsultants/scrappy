name := "OtoMotoML"

version := "1.0"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
    "org.jsoup" % "jsoup" % "1.10.2",
    "com.github.tototoshi" %% "scala-csv" % "1.3.5",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
    "org.slf4j" % "slf4j-api" % "1.7.5",
    "org.slf4j" % "slf4j-simple" % "1.7.5"
    //"org.apache.spark" % "spark-mllib_2.10" % "1.3.0" % "provided"
)
        