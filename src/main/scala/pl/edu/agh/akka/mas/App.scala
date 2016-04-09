package pl.edu.agh.akka.mas

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by novy on 06.04.16.
  */
object App {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty)
      startup(Seq("2553"))
    else
      startup(args)
  }

  def startup(ports: Seq[String]): Unit = {
    ports foreach { port =>
      // Override the configuration of the port
      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
        withFallback(ConfigFactory.load())

      // Create an Akka system
      val system = ActorSystem("ClusterSystem", config)
      // Create an actor that handles cluster domain events
      system.actorOf(Props[IslandTopologyCoordinator], name = "coord")
    }
  }
}
