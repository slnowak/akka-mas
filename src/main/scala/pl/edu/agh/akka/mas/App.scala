package pl.edu.agh.akka.mas

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory
import pl.edu.agh.akka.mas.cluster.management.IslandTopologyCoordinator
import pl.edu.agh.akka.mas.island.IslandActor
import pl.edu.agh.akka.mas.problems.{RastriginAgent, RastriginIsland}

/**
  * Created by novy on 06.04.16.
  */
object App {

  def main(args: Array[String]): Unit = {
    if (args.isEmpty)
      startup(Seq("2551", "2552"))
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
      val island: ActorRef = system.actorOf(RastriginIsland.props(), name = "island")
      system.actorOf(IslandTopologyCoordinator.props(island), name = "islandCoordinator")
    }
  }
}
