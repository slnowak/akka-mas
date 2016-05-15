package pl.edu.agh.akka.mas.island

import akka.actor._
import pl.edu.agh.akka.mas.cluster.management.IslandTopologyCoordinator.NeighboursChanged
import pl.edu.agh.akka.mas.island.AgentActor.RequestMigration
import pl.edu.agh.akka.mas.island.MigrationArena.{Agent, KillAgents, SpawnNewAgents}
import pl.edu.agh.akka.mas.problems.RastriginAgent.RastriginSolution

import scala.util.Random

/**
  * Created by novy on 10.04.16.
  */
class MigrationArena(var neighbourIslands: List[ActorSelection], thisIsland: ActorRef, requiredAgentsToMigrate: Int)
  extends Actor with ActorLogging {

  var agentsToMigrate: List[Agent] = List.empty

  override def receive: Receive = {
    case NeighboursChanged(newNeighbours) =>
      this.neighbourIslands = newNeighbours

    case RequestMigration(solution) if enoughAgentsGathered() =>
      log.info(s"$self: starting migration with agents: $agentsToMigrate to random neighbour from: $neighbourIslands")
      agentsToMigrate = Agent(solution, sender()) :: agentsToMigrate
      randomNeighbour() foreach migrate(agentsToMigrate reverse)
      agentsToMigrate = List.empty

    case RequestMigration(agentState) =>
      agentsToMigrate = Agent(agentState, sender()) :: agentsToMigrate
  }

  private def migrate(agentsToMigrate: List[Agent])(neighbour: ActorSelection): Unit = {
    neighbour ! SpawnNewAgents(agentsToMigrate.map(_.solution))
    thisIsland ! KillAgents(agentsToMigrate.map(_.agentActor))
  }

  private def enoughAgentsGathered(): Boolean = agentsToMigrate.size + 1 == requiredAgentsToMigrate

  private def randomNeighbour(): Option[ActorSelection] = Random.shuffle(neighbourIslands).headOption
}

object MigrationArena {

  def props(neighbours: List[ActorSelection], relatedIsland: ActorRef, requiredAgentsToMigrate: Int = 3): Props =
    Props(new MigrationArena(neighbours, relatedIsland, requiredAgentsToMigrate))

  case class SpawnNewAgents(initialSolutions: List[RastriginSolution])

  case class KillAgents(addresses: List[ActorRef])

  case class Agent(solution: RastriginSolution, agentActor: ActorRef)

}
