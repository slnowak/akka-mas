package pl.edu.agh.akka.mas.island

import akka.actor._
import pl.edu.agh.akka.mas.cluster.management.IslandTopologyCoordinator.NeighboursChanged
import pl.edu.agh.akka.mas.island.IslandActor.SpawnNewAgents
import pl.edu.agh.akka.mas.island.MigrationArena.{Agent, PerformMigration}
import pl.edu.agh.akka.mas.island.rastrigin.RastriginFeature

import scala.util.Random

/**
  * Created by novy on 10.04.16.
  */
class MigrationArena(neighbourIslands: List[ActorSelection], thisIsland: ActorRef)
  extends Actor with ActorLogging {

  override def receive: Receive = migrationArenaBehaviour(neighbourIslands, thisIsland)

  private def migrationArenaBehaviour(neighbourIslands: List[ActorSelection], thisIsland: ActorRef): Receive = {
    case NeighboursChanged(newNeighbours) =>
      context become migrationArenaBehaviour(newNeighbours, thisIsland)

    case PerformMigration(features) =>
      randomNeighbour() foreach migrate(features)
  }

  private def randomNeighbour(): Option[ActorSelection] = Random.shuffle(neighbourIslands).headOption

  private def migrate(agents: List[Agent])(neighbour: ActorSelection): Unit = {
    neighbour ! SpawnNewAgents(agents)
  }
}

object MigrationArena {

  def props(neighbours: List[ActorSelection], relatedIsland: ActorRef): Props =
    Props(new MigrationArena(neighbours, relatedIsland))


  case class KillAgents(addresses: List[ActorRef])

  case class PerformMigration(agents: List[Agent])

  case class Agent(feature: RastriginFeature)

}
