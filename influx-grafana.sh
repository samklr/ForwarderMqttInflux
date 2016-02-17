#!/usr/bin/env bash


docker run -d -p 8083:8083  \
              -p 8086:8086  \
              tutum/influxdb


docker run -d -p 80:80 \
  -e INFLUXDB_HOST=localhost \
  -e INFLUXDB_PORT=8086 \
  -e INFLUXDB_NAME=mqtt \
  -e INFLUXDB_USER=root \
  -e INFLUXDB_PASS=root \
  -e INFLUXDB_IS_GRAFANADB=true \
  tutum/grafana