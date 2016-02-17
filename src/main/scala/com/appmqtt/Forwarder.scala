package com.appmqtt

import java.util

import com.typesafe.config.ConfigFactory
import org.eclipse.paho.client.mqttv3._
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.influxdb.InfluxDB.{ConsistencyLevel, LogLevel}
import org.influxdb.{InfluxDB, InfluxDBFactory}
import org.influxdb.dto.BatchPoints


/**
  * Created by @samklr on 12/02/16.
  **/

object Forwarder {

  val config = ConfigFactory.load

  //Init Influx
  var influx : Option[InfluxDB] = None


  def main(args: Array[String]): Unit = {

    influx = initInflux

   //Read Broker and Influx params
    val brokerUrl = config.getString("app.broker.uri")

    // Init Connects to Broker and Influx
    val brokerClient = new MqttClient(brokerUrl, MqttClient.generateClientId, new MemoryPersistence)

    val connectionsOptions = new MqttConnectOptions
    connectionsOptions.setCleanSession(true)
    //connectionsOptions.setSSLProperties() //TODO Configure SSL Keystore
    connectionsOptions.setMqttVersion(3)
    //connectionsOptions.setWill() //TODO

    brokerClient.connect(connectionsOptions)

    // Get List of topics from Config
    val topics = config.getStringList("app.broker.topics").toArray(Array[String]())

    //subscribe to topics
    brokerClient.subscribe(topics)

    val callback = new MqttCallback {

      override def messageArrived(topic: String, message: MqttMessage): Unit = {
        println("Incoming Data... Topic : %s, Value : %s".format(topic, message))
        influxWriter(topic, message)
      }

      override def connectionLost(cause: Throwable): Unit = {
        println("Lost Connection" + cause)
      }

      override def deliveryComplete(token: IMqttDeliveryToken): Unit = {

      }
    }

    //Set up callback for MqttClient
    brokerClient.setCallback(callback)

    println ("Ready! Shoot !!!!! ")
  }


  def initInflux : Option[InfluxDB] = {
    //Get Parameters from application.conf

    val influxUrl = config.getString("app.influx.uri")
    val influxUser = config.getString("app.influx.user")
    val influxPassword= config.getString("app.influx.password")
    val influxDbName= config.getString("app.influx.db_name")

    val influx = InfluxDBFactory.connect(influxUrl, influxUser, influxPassword)
    var started = false

    while (!started){
      try{
         println ("Pinging InfluxDB")
         val pong = influx.ping
         println(pong)
         started = !pong.getVersion.equalsIgnoreCase("unknown")
      }
      catch {
        case e : Exception => e.printStackTrace
      }
    }

    influx.setLogLevel(LogLevel.BASIC);
    println("-----------------------------------------------------------------------------")
    println("# Connected to InfluxDB Version: " + influx.version + " #");
    println("-----------------------------------------------------------------------------")
    //TODO Handle None

    Some(influx)
  }

  def influxWriter(topic : String, message : MqttMessage) ={
    // Create a Point with the topic being the description
    if (message != null){
      //Message end with \0 in the mqtt payload so let's remove the interesting part

      val bytes = java.util.Arrays.copyOf(message.getPayload, message.getPayload.length - 1)
      val content = new String(bytes)

      // You can write using the string protocol  influx.write(influxDbName,"default", InfluxDB.ConsistencyLevel.ONE, topic+"serie="+content)

      //Instead here we're Using BatchPoint to write

      val batchPoints = BatchPoints.database(config.getString("app.influx.db_name"))
                                    .tag("async", "true")
                                    .consistency(ConsistencyLevel.ONE)
                                    .retentionPolicy("default")
                                    .build


      val point = org.influxdb.dto.Point.measurement(topic)
                                        .field("event",content)
                                        .build
      //val point2 .....

      //TODO Batch Multiple Points before Flushing to DB

      batchPoints.point(point)

      influx match {
        case Some(influxdb) => influxdb.write(batchPoints)
        case None => println("Not connected to Influx ... Dlushed to /dev/null")
      }

      //influx.write(batchPoints)

    }
  }

}

