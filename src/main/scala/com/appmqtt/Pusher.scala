package com.appmqtt


import java.util.concurrent.{Executors, TimeUnit}

import com.typesafe.config.ConfigFactory
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence


object Pusher {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()

    val brokerUri = config.getString("app.broker.uri")

    var client: MqttClient = null

    try {
      client = new MqttClient(brokerUri, MqttClient.generateClientId, new MemoryPersistence)
      client.connect

      val topics = config.getStringList("app.broker.topics")
                         .toArray(Array[String]())

      topics.foreach(
        topic => topicPusher(client, topic)
      )
    }
    catch {
      case ex : MqttException => ex.printStackTrace
    }

    sys addShutdownHook {
      println("closing Connection to the Broker")
      client.disconnect
      Thread.sleep(3000)
      println("Shutting down.")
    }

  }


  def topicPusher(client : MqttClient, topic : String) = {

    val executors = Executors.newScheduledThreadPool(1)

    val handler = executors.scheduleAtFixedRate(
      new Runnable {
        override def run(): Unit = {
          val random = scala.util.Random
          val temp = 10 + random.nextFloat() * 10

          client.getTopic(topic).publish(new MqttMessage(temp.toString.getBytes))
          println("Publishing Data, Topic : %s, Value : %s".format(topic, temp))

        }
      }, 0, 1, TimeUnit.SECONDS)

  }


}
