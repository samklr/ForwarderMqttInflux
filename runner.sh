#!/usr/bin/env bash

sbt/sbt assembly

export JAR_HOME="target/scala-2-11/app-mqtt-assembly-1.0.jar"

export GC_OPTIONS="-XX:+UseG1GC"

#nohup java -Xms512m -Xmx4096m $GC_OPTIONS -server -jar $JAR_HOME > /dev/null 

java  -server $GC_OPTIONS -Xms4096m -Xmx4096m -jar $JAR_HOME > /dev/null 2>&1 &
