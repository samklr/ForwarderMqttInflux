#!/usr/bin/env bash

export JAR_HOME="/home/samklr/code2/appmqtt/target/scala-2-11/app-mqtt-assembly-1.0.jar"

sbt/sbt assembly

export GC_OPTIONS="-XX:+UseG1GC"

#nohup java -Xms512m -Xmx4096m $GC_OPTIONS -server -jar $JAR_HOME > /dev/null 2>&1 &

java $GC_OPTIONS -Xms512m -Xmx4096m -server -jar $JAR_HOME > /dev/null 2>&1 &
