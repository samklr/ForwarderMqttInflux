
name := "app-mqtt"

version := "1.0"

scalaVersion := "2.11.7"

mainClass in assembly := Some("com.appmqtt.Forwarder")


libraryDependencies += "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.0.2"

libraryDependencies += "org.influxdb" % "influxdb-java" % "2.1"

libraryDependencies += "com.typesafe" % "config" % "1.3.0"



    