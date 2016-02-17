#!/usr/bin/env bash

sbt/sbt assembly

export JAR_HOME="target/scala-2-11/app-mqtt-assembly-1.0.jar"

export GC_OPTIONS="-XX:+UseG1GC"

#nohup java -Xms512m -Xmx4096m $GC_OPTIONS -server -jar $JAR_HOME > /dev/null 2>&1 &

java $GC_OPTIONS -Xms512m -Xmx4096m -server -jar $JAR_HOME > /dev/null 2>&1 &
