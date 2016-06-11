package pl.edu.agh.akka.mas

import akka.actor.{ActorSelection, ActorSystem}
import com.typesafe.config.ConfigFactory
import kamon.Kamon
import pl.edu.agh.akka.mas.cluster.management.IslandTopologyCoordinator
import pl.edu.agh.akka.mas.island.IslandActor

/**
  * Created by novy on 06.04.16.
  */
object App {

  def main(args: Array[String]): Unit = {
    if (args.isEmpty)
      startup(Seq("2551"))
    else
      startup(args)
  }

  def startup(ports: Seq[String]): Unit = {
    ports foreach { port =>
      // Override the configuration of the port
      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
        withFallback(ConfigFactory.load())

      val system = ActorSystem("ClusterSystem", config)
      system.actorOf(IslandActor.props(), "island")
      val neighbourhoodChangeSubscribers: ActorSelection = system.actorSelection("island/*")

      system.actorOf(
        IslandTopologyCoordinator.props(neighbourhoodChangeSubscribers),
        name = "islandCoordinator"
      )
    }
    Kamon.start()
  }
}
